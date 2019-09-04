package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.DebugOnly

@DebugOnly
class DebugActionObject(actionTime: Long = System.currentTimeMillis())
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_DEBUG, sourceFeed = "debug",
                   targetImageId = "debug", targetUserId = "debug", triggerStrategies = listOf(Immediate))
