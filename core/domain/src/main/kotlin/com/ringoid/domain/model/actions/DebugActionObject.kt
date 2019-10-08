package com.ringoid.domain.model.actions

import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.randomInt

@DebugOnly
class DebugActionObject(id: Int = randomInt(), actionTime: Long = System.currentTimeMillis())
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_DEBUG,
        sourceFeed = "debug",
        targetImageId = "debug", targetUserId = "debug",
        triggerStrategies = listOf(Immediate))
