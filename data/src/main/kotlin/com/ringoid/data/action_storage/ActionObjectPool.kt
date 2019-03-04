package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.repository.handleError
import com.ringoid.domain.action_storage.*
import com.ringoid.domain.model.actions.ActionObject
import com.ringoid.domain.model.actions.ViewActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor(private val cloud: RingoidCloud, private val spm: SharedPrefsManager,
                                           private val userScopeProvider: UserScopeProvider)
    : IActionObjectPool {

    companion object {
        private const val CAPACITY = 10
    }

    private val queue: Queue<ActionObject> = ArrayDeque()
    var lastActionTime: Long = 0L
        private set

    private val numbers = mutableMapOf<Class<ActionObject>, Int>()
    private val strategies = mutableMapOf<Class<ActionObject>, List<TriggerStrategy>>()
    private val timers = mutableMapOf<Class<ActionObject>, Disposable?>()

    private val triggerInProgress: AtomicInteger = AtomicInteger(0)

    // --------------------------------------------------------------------------------------------
    @Synchronized
    override fun put(aobj: ActionObject) {
        Timber.v("Put action object: $aobj")
        when (aobj) {
            is ViewActionObject -> { /* no-op */ }
            else -> {
                val viewAobj = ViewActionObject(actionTime = aobj.actionTime, sourceFeed = aobj.sourceFeed,
                    targetImageId = aobj.targetImageId, targetUserId = aobj.targetUserId,
                    triggerStrategies = emptyList() /* no trigger strategies for synthetic action object */)
                queue.offer(viewAobj)
            }
        }
        queue.offer(aobj)

        if (queue.size >= CAPACITY || aobj.triggerStrategies.contains(Immediate)) {
            Timber.v("Trigger immediately at $aobj")
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
                        .doOnComplete(this::trigger)
                        .subscribe({ Timber.v("Delay strategy has just satisfied at $aobj") }, Timber::e)
                }
        }
    }

    // ------------------------------------------
    @Synchronized @Suppress("CheckResult")
    override fun trigger() {
        if (queue.isEmpty()) {
            Timber.v("Triggering empty queue [0] - no-op")
            return
        }

        triggerSource()
            .autoDisposable(userScopeProvider)
            .subscribe({ Timber.d("Trigger Queue finished, last action time: $it") }, Timber::e)
        // TODO: hold disposable and retry it on recovery after retryWhen failed, w/o losing previous queue
    }

    override fun triggerSource(): Single<Long> {
        Timber.v("Start triggering source")
        triggerInProgress.getAndIncrement()
        return triggerSourceImpl()
            .repeatWhen {
                it.flatMap {
                    if (triggerInProgress.decrementAndGet() > 0) {
                        Timber.v("Repeat triggering source")
                        Flowable.just(it)
                    } else {
                        Timber.v("Finish triggering source")
                        Flowable.error(OnCompleteException())
                    }
                }
            }
            .single(lastActionTime)
            .onErrorResumeNext { e: Throwable ->
                if (e is OnCompleteException) {
                    Single.just(lastActionTime)
                } else {
                    Single.error(e)
                }
            }
    }

    private fun triggerSourceImpl(): Single<Long> {
        if (queue.isEmpty()) {
            Timber.v("Triggering empty queue [1] - no-op")
            return Single.just(lastActionTime)
        }

        lastActionTime = queue.peek()?.actionTime ?: 0L
        return spm.accessSingle { accessToken ->
            if (queue.isEmpty()) {
                Timber.v("Triggering empty queue [2] - no-op")
                Single.just(CommitActionsResponse(lastActionTime))
            } else {
                val essence = CommitActionsEssence(accessToken.accessToken, queue)
                cloud.commitActions(essence)
            }
        }
        .subscribeOn(Schedulers.io())
        .handleError()  // TODO: on fail - notify and restrict user from a any new aobjs until recovered
        .doOnSubscribe {
            // TODO: cache the queue to restore later in case of failure all retries
            Timber.d("Trigger Queue started. Queue size [${queue.size}], last action time: $lastActionTime, queue: ${printQueue()}")
            queue.clear()  // TODO: if no connection - clear queue at this stage leads to lost data
            numbers.clear()
            strategies.clear()
            timers.forEach { it.value?.dispose() }.also { timers.clear() }
        }
        .doOnSuccess {
            // TODO: queue.size == 0 always at this stage
            Timber.d("Successfully committed all [${queue.size}] actions, triggering has finished")
            lastActionTime = it.lastActionTime
        }
        .doOnDispose {
            Timber.d("Disposed trigger Queue, out of user scope: ${userScopeProvider.hashCode()}")
            lastActionTime = 0L  // drop 'lastActionTime' upon dispose, normally when 'user scope' is out
            triggerInProgress.set(0)
        }
        .map { it.lastActionTime }
    }

    // ------------------------------------------
    private fun printQueue(): String =
        queue.joinToString(", ", "[", "]", transform = { it.toActionString() })
}
