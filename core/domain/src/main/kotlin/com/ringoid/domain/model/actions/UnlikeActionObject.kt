package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.Immediate

@Deprecated("Since transition of profiles")
class UnlikeActionObject(actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_UNLIKE, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(Immediate))
