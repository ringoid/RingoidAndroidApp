package com.ringoid.data.action_storage

import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.repository.handleError
import com.ringoid.domain.action_storage.*
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.actions.ActionObject
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor(private val cloud: RingoidCloud,
    private val local: ActionObjectDao, private val mapper: ActionObjectDboMapper,
    private val spm: SharedPrefsManager, private val userScopeProvider: UserScopeProvider)
    : IActionObjectPool {

    companion object {
        private const val CAPACITY = 10
    }

    private val queue: Deque<OriginActionObject> = ArrayDeque()
    private val lastActionTimeValue = AtomicLong(0L)

    private val numbers = mutableMapOf<Class<OriginActionObject>, Int>()
    private val strategies = mutableMapOf<Class<OriginActionObject>, List<TriggerStrategy>>()
    private val timers = mutableMapOf<Class<OriginActionObject>, Disposable?>()

    private val triggerInProgress = TriggerSemaphore()

    init {
        lastActionTimeValue.set(spm.getLastActionTime())
    }

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

        if (queue.size >= CAPACITY || aobj.triggerStrategies.contains(Immediate)) {
            Timber.v("Trigger immediately at $aobj")
            DebugLogUtil.v("# Trigger by strategy: Immediate")
            trigger()  // trigger immediately
            return
        }

        aobj.javaClass.let { key ->
            numbers.takeIf { !it.containsKey(key) }
                ?.let { it[key] = 1 }  // first object of that type
                ?: run { numbers[key] = numbers[key]!! + 1 }

            /**
             * Check previous [CountFromLast] strategy for [aobj], if any. That strategy relies on
             * total number of [aobj] of the [key] class. So, if some previously added [aobj] had
             * set some strategy, need to check whether it is satisfied with the newly incoming [aobj],
             * and if so - trigger, otherwise just rewrite strategies for [aobj] of [key] class.
             *
             * [ActionObject.triggerStrategies] of an incoming [aobj] could differ from previous
             * strategies, for example - it may not contain [CountFromLast] strategy, i.e. cancelling
             * it for any future incoming objects.
             *
             * If the [CountFromLast] has been cancelled like that, but then just restored with the
             * newly incoming [aobj] such that it's satisfied at the same time (since the number
             * of [aobj] of [key] class hits the threshold set by that restored strategy), [trigger]
             * won't be called by design - it will be called next time a new [aobj] will come and
             * over satisfy the previously restored strategy.
             */
            strategies[key]  // previously stored strategies
                ?.find { it is CountFromLast }?.let { it as CountFromLast }
                ?.takeIf { it.count <= numbers[key] ?: 0 }  // test hit the threshold
                ?.let {
                    Timber.v("Count strategy has just satisfied at $aobj")
                    DebugLogUtil.v("# Trigger by strategy: CountFromLast")
                    trigger()
                    return
                }

            /**
             * Check whether [DelayFromLast] strategy is present - that means that previous object
             * had established that strategy and it's timer threshold. Since a new [aobj] is coming,
             * timer must be dropped to '0', because delay is always considered since last incoming
             * [aobj] of [key] class.
             *
             * [ActionObject.triggerStrategies] of an incoming [aobj] could differ from previous
             * strategies, for example - it may not contain [DelayFromLast] strategy, i.e. cancelling
             * it for any future incoming objects.
             *
             * If the [DelayFromLast] has been cancelled like that, corresponding timer is stopped
             * and hence - won't trigger until the strategy is restored. On strategy restore - new
             * threshold will be set and timer will start from '0', as normal.
             */
            strategies[key]  // previously stored strategies
                ?.find { it is DelayFromLast }
                ?.let {
                    // drop timer since new aobj comes up
                    timers[key]?.dispose()
                    timers[key] = null
                }

            strategies[key] = aobj.triggerStrategies  // update strategies from incoming aobj

            timers[key] = strategies[key]  // new strategies
                ?.find { it is DelayFromLast }?.let { it as DelayFromLast }
                ?.let {
                    // schedule timer to trigger after delay
                    Observable.timer(it.delay, TimeUnit.SECONDS)
                        .doOnComplete { DebugLogUtil.v("# Trigger by strategy: DelayFromLast") }
                        .doOnComplete(this::trigger)
                        .subscribe({ Timber.v("Delay strategy has just satisfied at $aobj") }, Timber::e)
                }
        }
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
//            .doOnSubscribe { DebugLogUtil.v("Thread [t=${tid - 1}] has entered commit actions section...") }
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

        return local.actionObjects()
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
        Completable.fromCallable { local.addActionObjects(queue.map(mapper::map)) }
            .doOnSubscribe { Timber.v("Started backup action objects' queue before triggering...") }
            .doOnComplete { Timber.v("Action objects' queue has been backup-ed, before triggering") }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    @Suppress("CheckResult")
    private fun dropBackupQueue(): Completable =
        Completable.fromCallable { local.deleteActionObjects() }
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
