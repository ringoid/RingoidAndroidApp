package com.ringoid.data.local.database.model.action_storage

import com.ringoid.domain.model.actions.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectDboMapper @Inject constructor() {

    fun map(aobj: OriginActionObject): ActionObjectDbo {
        val dbo = ActionObjectDbo.from(aobj)
        return when (aobj) {
            is ActionObject -> {
                val xDbo = when (aobj) {
                    is BlockActionObject -> dbo.apply { blockReasonNumber = aobj.numberOfBlockReason }
                    is MessageActionObject -> dbo.apply { messageText = aobj.text }
                    is OpenChatActionObject -> dbo.apply { openChatTimeMillis = aobj.timeInMillis }
                    is ViewChatActionObject -> dbo.apply { viewChatTimeMillis = aobj.timeInMillis }
                    is ViewActionObject -> dbo.apply { viewTimeMillis = aobj.timeInMillis }
                    else -> dbo
                }
                xDbo.apply {
                    sourceFeed = aobj.sourceFeed
                    targetImageId = aobj.targetImageId
                    targetUserId = aobj.targetUserId
                }
            }
            else -> dbo
        }
    }
}
