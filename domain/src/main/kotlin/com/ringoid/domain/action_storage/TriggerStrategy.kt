package com.ringoid.domain.action_storage

sealed class TriggerStrategy

object Immediate : TriggerStrategy()
class CountFromLast(val count: Int = LIKE_COUNT_ON_TRIGGER) : TriggerStrategy()
class DelayFromLast(val delay: Long = LIKE_DELAY_ON_TRIGGER) : TriggerStrategy()  // in seconds

const val LIKE_COUNT_ON_TRIGGER = 5
const val LIKE_DELAY_ON_TRIGGER = 5L  // in seconds
const val VIEW_DELAY_ON_TRIGGER = 10L
