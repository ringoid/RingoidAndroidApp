package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.BaseActionObject
import com.ringoid.domain.model.actions.OriginActionObject

/**
 * [IActionObjectPool] will analyze [TriggerStrategy] for each incoming [BaseActionObject] and
 * commit action objects when strategy fulfills.
 */
sealed class TriggerStrategy {

    override fun toString(): String = javaClass.simpleName
}

/**
 * [IActionObjectPool] won't commit actions automatically while [BaseActionObject] with
 * this strategy are put into, until some action object with another strategy comes.
 *
 * @note: this has the same effect as leaving [OriginActionObject.triggerStrategies] empty.
 */
object NoAction : TriggerStrategy()

/**
 * [IActionObjectPool] will commit action objects immediately once [BaseActionObject] has been put into.
 */
object Immediate : TriggerStrategy()

/**
 * [IActionObjectPool] will commit action objects when there are at least [count] items have been put into.
 */
class CountFromLast(val count: Int = COUNT_ON_TRIGGER) : TriggerStrategy() {

    override fun toString(): String = "${super.toString()}(count=$count)"
}

/**
 * [IActionObjectPool] will commit action objects when a specific [delay] will have been passed since
 * the moment the first action object with such strategy has been put into.
 */
class DelayFromLast(val delay: Long = DELAY_ON_TRIGGER) : TriggerStrategy() {  // in seconds

    override fun toString(): String = "${super.toString()}(delay=$delay)"
}

const val COUNT_ON_TRIGGER = 5
const val DELAY_ON_TRIGGER = 3L  // in seconds
const val VIEW_DELAY_ON_TRIGGER = 2L  // in seconds
