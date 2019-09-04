package com.ringoid.datainterface.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.utility.notBlankOf

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "repeatRequestAfter":0
 * }
 */
open class BaseResponse(
    @Expose @SerializedName(COLUMN_ERROR_CODE) val errorCode: String = "",
    @Expose @SerializedName(COLUMN_ERROR_MESSAGE) val errorMessage: String = "",
    @Expose @SerializedName(COLUMN_REPEAT_AFTER) val repeatRequestAfter: Long = 0L,
    @Expose @SerializedName(COLUMN_REQUEST_URL) val requestUrl: String? = null,  // filled only for fatal network errors
    @Expose @SerializedName(COLUMN_UNEXPECTED) val unexpected: String? = null) : IEssence {

    companion object {
        const val COLUMN_ERROR_CODE = "errorCode"
        const val COLUMN_ERROR_MESSAGE = "errorMessage"
        const val COLUMN_REPEAT_AFTER = "repeatRequestAfter"
        const val COLUMN_REQUEST_URL = "requestUrl"
        const val COLUMN_UNEXPECTED = "unexpected"
    }

    fun isSuccessful(): Boolean =
        (errorCode.isNullOrBlank() || errorCode == "null") &&
        (errorMessage.isNullOrBlank() || errorMessage == "null") &&
        (unexpected.isNullOrBlank() || unexpected == "null")

    fun errorString(): String = if (!isSuccessful()) notBlankOf(errorMessage, unexpected) else ""
    open fun toLogString(): String = ""

    override fun toString(): String =
        "BaseResponse(errorCode='$errorCode', errorMessage='$errorMessage', repeatRequestAfter=$repeatRequestAfter, requestUrl='$requestUrl', unexpected='$unexpected')"
}
