package com.ringoid.datainterface.remote.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.datainterface.remote.model.BaseResponse

/**
 * {
 *   "lastActionTime":123456,
 *   "errorCode":"",
 *   "errorMessage":""
 * }
 */
class CommitActionsResponse(
    @Expose @SerializedName(COLUMN_LAST_ACTION_TIME) val lastActionTime: Long,
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec) {

    companion object {
        const val COLUMN_LAST_ACTION_TIME = "lastActionTime"
    }

    override fun toLogString(): String = "lastActionTime=$lastActionTime"

    override fun toString(): String = "CommitActionsResponse(lastActionTime=$lastActionTime, ${super.toString()})"
}
