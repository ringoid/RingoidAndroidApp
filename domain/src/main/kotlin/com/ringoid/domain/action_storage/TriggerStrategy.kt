package com.ringoid.domain.action_storage

sealed class TriggerStrategy

object Immediate : TriggerStrategy()
class CountFromLast(count: Int = LIKE_COUNT_ON_TRIGGER) : TriggerStrategy()
class DelayFromLast(delay: Int = LIKE_DELAY_ON_TRIGGER) : TriggerStrategy()  // in seconds

const val LIKE_COUNT_ON_TRIGGER = 5
const val LIKE_DELAY_ON_TRIGGER = 5  // in seconds
const val VIEW_DELAY_ON_TRIGGER = 10
