package com.ringoid.data.action_storage

import com.ringoid.data.handleError
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.PerBackup
import com.ringoid.datainterface.local.action_storage.IActionObjectDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.actions.CommitActionsResponse
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor(
    @PerBackup private val backup: IActionObjectDbFacade,
    private val userScopeProvider: UserScopeProvider,
    cloud: IRingoidCloudFacade, spm: SharedPrefsManager)
    : BarrierActionObjectPool(cloud, spm) {

    private val queue: Deque<OriginActionObject> = ArrayDeque()

    // ------------------------------------------------------------------------
    override fun getTotalQueueSize(): Int = queue.size

    override fun countActionObjects(): Single<Int> = Single.just(queue.size)

    // --------------------------------------------------------------------------------------------
    @Synchronized
    override fun put(aobj: OriginActionObject, onComplete: (() -> Unit)?) {
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
        onComplete?.invoke()
    }

    @Synchronized
    override fun put(aobjs: Collection<OriginActionObject>, onComplete: (() -> Unit)?) {
        Timber.v("Put action objects [${aobjs.size}]: ${aobjs.joinToString()}")
        aobjs.forEach { aobj ->
            queue.offer(aobj)
            analyzeActionObject(aobj)
        }
        onComplete?.invoke()
    }

    // ------------------------------------------
    @Synchronized @Suppress("CheckResult")
    override fun trigger() {
        Timber.d("Trigger standalone")
        if (queue.isEmpty()) {
            DebugLogUtil.d("No actions to commit, lAt is up-to-date [standalone]")
            return  // do nothing on empty queue
        }

        triggerSource()
            .subscribeOn(Schedulers.io())
            .autoDisposable(userScopeProvider)
            .subscribe({ Timber.d("Trigger Queue finished, last action time: $it") }, Timber::e)
    }

    override fun triggerSourceImpl(): Single<Long> {
        val localLastActionTime = queue.maxBy { it.actionTime }?.actionTime ?: lastActionTime()
        val backupQueue: Deque<OriginActionObject> = ArrayDeque()

        val source = spm.accessSingle { accessToken ->
            if (queue.isEmpty()) {
                DebugLogUtil.d("No actions to commit, lAt is up-to-date [chained]")
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
        .doOnError { DebugLogUtil.e("Commit actions error: $it") }
        .handleError(tag = "commitActions", traceTag = "actions/actions")
        .doOnSubscribe {
            Timber.d("Trigger Queue started. Queue size [${queue.size}], last action time: ${lastActionTime()}, queue: ${printQueue()}")
            backupQueue.addAll(queue)
            queue.clear()
            dropStrategyData()
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
            DebugLogUtil.d("Commit actions disposed [user scope: ${userScopeProvider.hashCode()}]")
            finalizePool()  // clear state of pool
        }
        .doOnError { backupQueue(backupQueue) }
        .doFinally { backupQueue.clear() }
        .map { it.lastActionTime }

        return backup.actionObjects()
            .flatMapCompletable {
                it.reversed().forEach { queue.offerFirst(it) }
                DebugLogUtil.v("Restored [${it.size}] action objects from backup, total: ${queue.size}")
                dropBackupQueue()
            }
            .toSingleDefault(0L)
            .flatMap { source }
    }

    @Suppress("CheckResult")
    override fun finalizePool() {
        super.finalizePool()
        dropBackupQueue()
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun backupQueue(queue: Deque<OriginActionObject>) {
        Completable.fromCallable { backup.addActionObjects(queue.toList()) }
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
}
