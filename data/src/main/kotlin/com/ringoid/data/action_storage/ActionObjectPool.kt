package com.ringoid.data.action_storage

import com.ringoid.data.di.PerBackup
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.repository.handleError
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor(
    cloud: RingoidCloud, @PerBackup private val backup: ActionObjectDao,
    private val local: ActionObjectDao, private val mapper: ActionObjectDboMapper,
    private val spm: SharedPrefsManager, private val userScopeProvider: UserScopeProvider)
    : BaseActionObjectPool(cloud) {

    private val queue: Deque<OriginActionObject> = ArrayDeque()
    private val lastActionTimeValue = AtomicLong(0L)

    private val triggerInProgress = TriggerSemaphore()

    init {
        lastActionTimeValue.set(spm.getLastActionTime())
    }

    // ------------------------------------------------------------------------
    override fun getTotalQueueSize(): Int = queue.size

    // --------------------------------------------------------------------------------------------
    @Synchronized
    override fun put(aobj: OriginActionObject) {
        Timber.v("Put action object: $aobj")
//        when (aobj) {
//            is ViewActionObject, is ViewChatActionObject -> { /* no-op */ }
//            else -> {
//                val viewAobj = ViewActionObject(actionTime = aobj.actionTime - 1, sourceFeed = aobj.sourceFeed,
//                    targetImageId = aobj.targetImageId, targetUserId = aobj.targetUserId,
//                    triggerStrategies = emptyList() /* no trigger strategies for synthetic action object */)
//                queue.offer(viewAobj)
//            }
//        }
        queue.offer(aobj)
        analyzeActionObject(aobj)
    }

    // ------------------------------------------
    @Synchronized @Suppress("CheckResult")
    override fun trigger() {
        if (queue.isEmpty()) {
            return
        }

        triggerSource()
            .autoDisposable(userScopeProvider)
            .subscribe({ Timber.d("Trigger Queue finished, last action time: $it") }, Timber::e)
    }

    override fun triggerSource(): Single<Long> =
        Single.just(ProcessingPayload(threadId = tid++) to tcount++)
            .flatMap { thread ->
                if (triggerInProgress.isLocked()) {
                    Single.error(WaitUntilTriggerFinishedException(tpayload = thread.first))
                } else {
                    triggerInProgress.increment()
                    triggerSourceImpl()
                        .doOnSubscribe { DebugLogUtil.d("Commit actions started by [t=${thread.first.threadId}] at ${thread.first.startTime % 1000000} ms, queue: ${if (queue.isEmpty()) "empty" else queue.joinToString("\n\t\t", "\n\t\t", "", transform = { it.toActionString() })}") }
                        .doFinally {
                            triggerInProgress.decrement()
                            --tcount
                            DebugLogUtil.d("Commit actions has finished by [t=${thread.first.threadId}], elapsed time ${System.currentTimeMillis() - thread.first.startTime} ms")
                        }
                }
            }
            .retryWhen {
                it.flatMap { e ->
                    if (e is WaitUntilTriggerFinishedException) {
                        DebugLogUtil.v("${e.message}, count $tcount")
                        Flowable.timer(200L, TimeUnit.MILLISECONDS)  // repeat
                    } else {
                        DebugLogUtil.e(e)
                        Flowable.error(e)
                    }
                }
            }

    private var tid: Long = 0L  // thread id
    private var tcount: Long = 0L  // count of threads

    private fun triggerSourceImpl(): Single<Long> {
        val localLastActionTime = queue.maxBy { it.actionTime }?.actionTime ?: lastActionTime()
        val backupQueue: Deque<OriginActionObject> = ArrayDeque()

        val source = spm.accessSingle { accessToken ->
            if (queue.isEmpty()) {
                Single.just(CommitActionsResponse(lastActionTime()))
            } else {
                /**
                 * The Gson object is threadsafe, but serialization of user objects is not.
                 * We should either make a defensive copy or use a lock when an object needs
                 * serialized but can also be manipulated from other threads concurrently.
                 *
                 * This is the case when queue is being committed and populated from other places
                 * at the same time. So use defensive copy here for serialization.
                 *
                 * @see https://github.com/google/gson/issues/1159
                 */
                val queueCopy = ArrayDeque(queue)
                val essence = CommitActionsEssence(accessToken.accessToken, queueCopy)
                cloud.commitActions(essence)
            }
        }
        .handleError(tag = "commitActions")
        .doOnSubscribe {
            Timber.d("Trigger Queue started. Queue size [${queue.size}], last action time: ${lastActionTime()}, queue: ${printQueue()}")
            backupQueue.addAll(queue)
            queue.clear()
            numbers.clear()
            strategies.clear()
            timers.forEach { it.value?.dispose() }.also { timers.clear() }
        }
        .doOnSuccess {
            // Queue.size == 0 always at this stage
            Timber.d("Successfully committed all [${queue.size}] actions, triggering has finished")
            if (localLastActionTime != it.lastActionTime) {
                Timber.w("Last action times differ: server=${it.lastActionTime}, client=${lastActionTime()}, delta=${it.lastActionTime - lastActionTime()}")
                SentryUtil.w("Last action time from Server differs from Client",
                    listOf("server last action time" to "${it.lastActionTime}",
                           "client last action time" to "${lastActionTime()}"))
            }
            updateLastActionTime(it.lastActionTime)
        }
        .doOnDispose {
            Timber.d("Disposed trigger Queue, out of user scope: ${userScopeProvider.hashCode()}")
            finalizePool()  // clear state of pool
        }
        .doOnError { backupQueue(backupQueue) }
        .doFinally { backupQueue.clear() }
        .map { it.lastActionTime }

        return backup.actionObjects()
            .subscribeOn(Schedulers.io())
            .map { it.mapList() }
            .flatMapCompletable {
                it.reversed().forEach { queue.offerFirst(it) }
                DebugLogUtil.v("Restored [${it.size}] action objects from backup, total: ${queue.size}")
                dropBackupQueue()
            }
            .toSingleDefault(0L)
            .flatMap { source }
    }

    override fun lastActionTime(): Long = lastActionTimeValue.get()

    @Suppress("CheckResult")
    override fun finalizePool() {
        updateLastActionTime(0L)  // drop 'lastActionTime' upon dispose, normally when 'user scope' is out
        triggerInProgress.drop()
        dropBackupQueue()
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun backupQueue(queue: Deque<OriginActionObject>) {
        Completable.fromCallable { backup.addActionObjects(queue.map(mapper::map)) }
            .doOnSubscribe { Timber.v("Started backup action objects' queue before triggering...") }
            .doOnComplete { Timber.v("Action objects' queue has been backup-ed, before triggering") }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    @Suppress("CheckResult")
    private fun dropBackupQueue(): Completable =
        Completable.fromCallable { backup.deleteActionObjects() }
            .doOnSubscribe { Timber.v("Started to drop backup of action objects' queue after triggered...") }
            .doOnComplete { Timber.v("Action objects' queue backup has been dropped after triggered") }

    private fun printQueue(): String =
        queue.joinToString(", ", "[", "]", transform = { it.toActionString() })

    // ------------------------------------------
    private fun updateLastActionTime(lastActionTime: Long) {
        lastActionTimeValue.set(lastActionTime)
        if (lastActionTime == 0L) {
            spm.deleteLastActionTime()
        } else {
            spm.saveLastActionTime(lastActionTime)
        }
    }
}
