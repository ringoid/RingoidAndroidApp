package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.CountFromLast
import com.ringoid.domain.action_storage.DelayFromLast

class UnlikeActionObject(actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String)
    : ActionObject(actionTime = actionTime, actionType = "UNLIKE", sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = listOf(CountFromLast(), DelayFromLast()))
