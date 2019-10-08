package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

@Deprecated("Since transition of profiles")
class UnlikeActionObject(
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_UNLIKE,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate))
