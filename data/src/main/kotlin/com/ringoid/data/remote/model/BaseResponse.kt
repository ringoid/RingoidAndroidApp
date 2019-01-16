package com.ringoid.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BaseResponse(
    @Expose @SerializedName(COLUMN_ERROR_CODE) val errorCode: String = "",
    @Expose @SerializedName(COLUMN_ERROR_MESSAGE) val errorMessage: String = "") {

    companion object {
        const val COLUMN_ERROR_CODE = "errorCode"
        const val COLUMN_ERROR_MESSAGE = "errorMessage"
    }

    override fun toString(): String {
        return "BaseResponse(errorCode='$errorCode', errorMessage='$errorMessage')"
    }
}
