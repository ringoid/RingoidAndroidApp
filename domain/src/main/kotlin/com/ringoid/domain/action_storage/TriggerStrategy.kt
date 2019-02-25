package com.ringoid.domain.action_storage

sealed class TriggerStrategy {

    override fun toString(): String = javaClass.simpleName
}

object Immediate : TriggerStrategy()

class CountFromLast(val count: Int = LIKE_COUNT_ON_TRIGGER) : TriggerStrategy() {

    override fun toString(): String = "${super.toString()}(count=$count)"
}

class DelayFromLast(val delay: Long = LIKE_DELAY_ON_TRIGGER) : TriggerStrategy() {  // in seconds

    override fun toString(): String = "${super.toString()}(delay=$delay)"
}

const val LIKE_COUNT_ON_TRIGGER = 5
const val LIKE_DELAY_ON_TRIGGER = 3L  // in seconds
const val VIEW_DELAY_ON_TRIGGER = 2L  // in seconds
