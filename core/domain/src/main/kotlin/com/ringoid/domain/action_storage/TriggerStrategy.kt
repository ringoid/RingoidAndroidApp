package com.ringoid.domain.action_storage

import com.ringoid.domain.model.actions.BaseActionObject

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
 */
object NoAction : TriggerStrategy()

/**
 * [IActionObjectPool] will commit action objects immediately once [BaseActionObject] has been put into.
 */
object Immediate : TriggerStrategy()

/**
 * [IActionObjectPool] will commit action objects when there are at least [count] items have been put into.
 */
class CountFromLast(val count: Int = LIKE_COUNT_ON_TRIGGER) : TriggerStrategy() {

    override fun toString(): String = "${super.toString()}(count=$count)"
}

/**
 * [IActionObjectPool] will commit action objects when a specific [delay] will have been passed since
 * the moment the first action object with such strategy has been put into.
 */
class DelayFromLast(val delay: Long = LIKE_DELAY_ON_TRIGGER) : TriggerStrategy() {  // in seconds

    override fun toString(): String = "${super.toString()}(delay=$delay)"
}

const val LIKE_COUNT_ON_TRIGGER = 5
const val LIKE_DELAY_ON_TRIGGER = 3L  // in seconds
const val VIEW_DELAY_ON_TRIGGER = 2L  // in seconds
