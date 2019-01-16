package com.ringoid.data.remote.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "lastActionTime":123456,
 *   "errorCode":"",
 *   "errorMessage":""
 * }
 */
class CommitActionsResponse(
    @Expose @SerializedName(COLUMN_LAST_ACTION_TIME) val lastActionTime: Long,
    errorCode: String = "", errorMessage: String = "")
    : BaseResponse(errorCode, errorMessage) {

    companion object {
        const val COLUMN_LAST_ACTION_TIME = "lastActionTime"
    }
}
