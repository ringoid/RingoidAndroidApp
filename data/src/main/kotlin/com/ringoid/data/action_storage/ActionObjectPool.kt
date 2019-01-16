package com.ringoid.data.action_storage

import com.ringoid.domain.action_storage.*
import com.ringoid.domain.model.actions.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor() : IActionObjectPool {

    companion object {
        private const val CAPACITY = 100
    }

    private val queue: Queue<ActionObject> = ArrayDeque()

    private val numbers = mutableMapOf<Class<ActionObject>, Int>()
    private val strategies = mutableMapOf<Class<ActionObject>, List<TriggerStrategy>>()
    private val timers = mutableMapOf<Class<ActionObject>, Int>()

    @Synchronized
    override fun put(aobj: ActionObject) {
        Timber.v("Put action object: $aobj")
        queue.offer(aobj)

        if (queue.size >= CAPACITY || aobj.triggerStrategies.contains(Immediate)) {
            Timber.v("Trigger immediately at $aobj")
            trigger()  // trigger immediately
            return
        }

        aobj.javaClass.let { key ->
            numbers.takeIf { !it.containsKey(key) }
                ?.let { it[key] = 1 }  // first object of that type
                ?: run { numbers[key]!!.plus(1) }

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
                ?.takeIf { it.count > numbers[key] ?: 0 }  // test hit the threshold
                ?.let { Timber.v("Count strategy not satisfied yet at $aobj") }
                ?: run {
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
                ?.let { timers[key] = 0 }  // drop timer since new aobj comes up

            strategies[key] = aobj.triggerStrategies  // update strategies from incoming aobj
            // TODO set delay threshold
        }

        when (aobj) {
            is BlockActionObject -> {}
            is LikeActionObject, is UnlikeActionObject -> {}
            is MessageActionObject -> {}
            is OpenChatActionObject -> {}
            is ViewActionObject -> {}
        }
    }

    @Synchronized
    override fun trigger() {
        Timber.v("Triggering...")
        // TODO: send request of the whole queue
        queue.clear()
        numbers.clear()
        strategies.clear()
    }
}
