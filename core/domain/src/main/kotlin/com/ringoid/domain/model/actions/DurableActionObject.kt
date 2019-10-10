package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.utility.randomInt

open class DurableActionObject(
    override var timeInMillis: Long = 1L,
    override var isHidden: Boolean = false,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(), actionType: String,
    sourceFeed: String,
    targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = emptyList())
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = actionType,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = triggerStrategies), IDurableAction {

    fun advance(): DurableActionObject {
        timeInMillis = System.currentTimeMillis() - actionTime
        return this
    }
}
