package com.ringoid.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import okhttp3.HttpUrl

open class BaseResponse(
    @Expose @SerializedName(COLUMN_ERROR_CODE) val errorCode: String = "",
    @Expose @SerializedName(COLUMN_ERROR_MESSAGE) val errorMessage: String = "",
    @Expose @SerializedName(COLUMN_REPEAT_AFTER_SEC) val repeatAfterSec: Long = 0L,
    @Expose @SerializedName(COLUMN_REQUEST_URL) val requestUrl: HttpUrl? = null,  // filled only for fatal network errors
    @Expose @SerializedName(COLUMN_UNEXPECTED) val unexpected: String? = null) : IEssence {

    companion object {
        const val COLUMN_ERROR_CODE = "errorCode"
        const val COLUMN_ERROR_MESSAGE = "errorMessage"
        const val COLUMN_REPEAT_AFTER_SEC = "repeatRequestAfterSec"
        const val COLUMN_REQUEST_URL = "requestUrl"
        const val COLUMN_UNEXPECTED = "unexpected"
    }

    override fun toString(): String {
        return "BaseResponse(errorCode='$errorCode', errorMessage='$errorMessage', requestUrl='$requestUrl', unexpected='$unexpected')"
    }
}
