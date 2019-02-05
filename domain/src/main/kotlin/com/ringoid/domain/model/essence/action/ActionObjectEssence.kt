package com.ringoid.domain.model.essence.action

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class ActionObjectEssence(
    @Expose @SerializedName(COLUMN_ACTION_TIME) val actionTime: Long = System.currentTimeMillis(),
    @Expose @SerializedName(COLUMN_ACTION_TYPE) val actionType: String,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: String,
    @Expose @SerializedName(COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @Expose @SerializedName(COLUMN_TARGET_USER_ID) val targetUserId: String) : IEssence {

    companion object {
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"
    }

    override fun toSentryPayload(): String = "[actionTime=$actionTime, actionType=$actionType, sourceFeed=$sourceFeed, targetPhotoId=$targetImageId, targetUserId=$targetUserId]"
}
