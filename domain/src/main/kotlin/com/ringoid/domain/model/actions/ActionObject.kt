package com.ringoid.domain.model.actions

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

sealed class BaseActionObject

/**
 * {
 *   "sourceFeed":"new_faces",  // who_liked_me, matches, messages
 *   "actionType":"BLOCK",
 *   "targetUserId":"skdfkjhkjsdhf",
 *   "targetPhotoId":"sldfnlskdj",
 *   "actionTime":12342342354  // unix time
 * }
 */
open class ActionObject(
    @SerializedName(COLUMN_ACTION_TIME) val actionTime: Long,
    @SerializedName(COLUMN_ACTION_TYPE) val actionType: String,
    @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: String,
    @SerializedName(COLUMN_TARGET_IMAGE_ID) val targetImageId: String,
    @SerializedName(COLUMN_TARGET_USER_ID) val targetUserId: String)
    : BaseActionObject(), IEssence {

    companion object {
        const val COLUMN_ACTION_TIME = "actionTime"
        const val COLUMN_ACTION_TYPE = "actionType"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_TARGET_IMAGE_ID = "targetPhotoId"
        const val COLUMN_TARGET_USER_ID = "targetUserId"
    }
}
