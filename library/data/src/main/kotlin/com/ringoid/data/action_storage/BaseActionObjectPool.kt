package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.action_storage.*
import com.ringoid.domain.model.actions.ActionObject
import com.ringoid.domain.model.actions.DurableActionObject
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.actions.ViewActionObject
import com.ringoid.report.log.Report
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

abstract class BaseActionObjectPool(protected val cloud: IRingoidCloudFacade, protected val spm: SharedPrefsManager)
    : IActionObjectPool {

    companion object {
        private const val CAPACITY = 10
    }

    private val lastActionTimeValue = AtomicLong(0L)

    init {
        lastActionTimeValue.set(spm.getLastActionTime())
//        if (BuildConfig.DEBUG) {
//            ThreadMonitor().start()
//        }
    }

    protected abstract fun getTotalQueueSize(): Int

    /**
     * Analyzes [aobj] and creates a companion [ViewActionObject] for it, if need.
     */
    protected fun createSyntheticViewActionObjectFor(aobj: OriginActionObject): OriginActionObject? =
        when (aobj) {
            is DurableActionObject -> null  // VIEW doesn't require to be followed by durable action objects
            is ActionObject ->
                ViewActionObject(
                    actionTime = aobj.actionTime - 1,
                    sourceFeed = aobj.sourceFeed,
                    targetImageId = aobj.targetImageId,
                    targetUserId = aobj.targetUserId,
                    triggerStrategies = emptyList()  /** no trigger strategies for synthetic action object */)
            else -> null
        }

    /**
     * Analyzes [aobjs] list and creates list of companion [ViewActionObject] each complementing
     * corresponding [ActionObject] from [aobjs] for a given unique pairs of [ActionObject.targetImageId]
     * and [ActionObject.targetUserId].
     */
    protected fun createSyntheticViewActionObjectsFor(aobjs: Collection<OriginActionObject>): Collection<OriginActionObject> {
        val list = mutableListOf<OriginActionObject>()
        val pairs = mutableMapOf<Pair<String, String>, ActionObject>()
        aobjs.forEach { aobj ->
            when (aobj) {
                is ActionObject -> pairs[aobj.key()] = aobj
            }
        }
        pairs.entries.forEach { (_, aobj) ->
            createSyntheticViewActionObjectFor(aobj)?.let { list.add(it) }
        }
        return list
    }

    // --------------------------------------------------------------------------------------------
    private val numbers = mutableMapOf<Class<OriginActionObject>, Int>()
    private val strategies = mutableMapOf<Class<OriginActionObject>, List<TriggerStrategy>>()
    private val timers = mutableMapOf<Class<OriginActionObject>, Disposable?>()

    protected fun dropStrategyData() {
        numbers.clear()
        strategies.clear()
        timers.forEach { it.value?.dispose() }.also { timers.clear() }
    }

    /**
     * Analyzes every incoming [ActionObject], in particular it's [ActionObject.triggerStrategies]
     * and when some criteria fulfilled then calls to [trigger] based on those strategies.
     *
     * Note that data structures that keep some data for this are not actually persisted,
     * but some implementations of [BaseActionObjectPool] could persist the queue of [ActionObject]s.
     * This data is lost on app restart, so in order to launch that strategies again,
     * a new [ActionObject] should be put into this pool, and no [ActionObject]s that was persisted
     * before will participate in such strategies.
     */
    @Synchronized
    protected fun analyzeActionObject(aobj: OriginActionObject) {
        if (aobj.triggerStrategies.isEmpty() || aobj.triggerStrategies.all { it is NoAction }) {
            return  // don't analyze and let some another caller to trigger
        }

        if (getTotalQueueSize() >= CAPACITY || aobj.triggerStrategies.contains(Immediate)) {
            Timber.v("Trigger immediately at $aobj")
            DebugLogUtil.v("# Trigger by strategy: Immediate")
            trigger()  // trigger immediately
            return
        }

        analyzeActionObjectImpl(aobj)  // analyze more complex trigger strategies
    }

    @Synchronized
    protected fun analyzeActionObjects(aobjs: Collection<OriginActionObject>) {
        aobjs.filter { aobj -> aobj.triggerStrategies.isNotEmpty() && !aobj.triggerStrategies.all { it is NoAction } }
             .let { list ->
                 list.find { it.triggerStrategies.contains(Immediate) }
                     ?.let {
                         Timber.v("Trigger batch immediately at $it")
                         DebugLogUtil.v("# Trigger batch by strategy: Immediate")
                         trigger()  // trigger the whole batch immediately
                         emptyList<OriginActionObject>()  // nothing to analyze further
                     } ?: list  // all aobjs have some more complex trigger strategies to be analyzed
             }
             // doesn't iterate over empty list
             .forEach { aobj -> analyzeActionObjectImpl(aobj) }  // analyze each aobj separately
    }

    private fun analyzeActionObjectImpl(aobj: OriginActionObject) {
        // start analyze action object with some more complex trigger strategies
        aobj.javaClass.let { key ->
            /**
             * Increment count of action objects of type as the current one.
             * This could be used for [CountFromLast] trigger strategy criterion.
             */
            numbers.takeIf { !it.containsKey(key) }
                ?.let { it[key] = 1 }  // first object of that type
                ?: run { numbers[key] = (numbers[key] ?: 0) + 1 }

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
                ?.takeIf { it.count <= (numbers[key] ?: 0) }  // test hit the threshold
                ?.let {
                    Timber.v("Count strategy has just satisfied at $aobj")
                    DebugLogUtil.v("# Trigger by strategy: CountFromLast")
                    trigger()  // trigger by Count strategy
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
                        .doOnComplete {
                            Timber.v("Delay strategy has just satisfied at $aobj")
                            DebugLogUtil.v("# Trigger by strategy: DelayFromLast")
                        }
                        .subscribe({ trigger() /** trigger by Delay strategy */ }, Timber::e)
                }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun finalizePool() {
        Timber.v("Finalizing pool")
        Report.breadcrumb("Finalized pool")
        updateLastActionTime(0L)  // drop 'lastActionTime' upon dispose, normally when 'user scope' is out
    }

    override fun isLastActionTimeValid(): Boolean = lastActionTime() > 0

    override fun lastActionTime(): Long = lastActionTimeValue.get()

    protected fun updateLastActionTime(lastActionTime: Long) {
        val prev = lastActionTime()
        if (prev > lastActionTime) {
            DebugLogUtil.v("Update last action time for lesser value")
//            Report.d("Update last action time for lesser value", extras = listOf("lAt" to "$lastActionTime", "prev lAt" to "$prev"))
        } else if (prev < lastActionTime) {
            lastActionTimeValue.set(lastActionTime)
            spm.saveLastActionTime(lastActionTime)
        }  // no-op on equal

        if (lastActionTime == 0L) {
            spm.deleteLastActionTime()
        }
        Report.breadcrumb("Commit actions success", "lastActionTime" to "$lastActionTime")
    }
}
