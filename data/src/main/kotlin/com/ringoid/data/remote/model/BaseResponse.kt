package com.ringoid.data.remote.model

import com.google.gson.annotations.SerializedName

open class BaseResponse(
    @SerializedName(COLUMN_ERROR_CODE) val errorCode: String = "",
    @SerializedName(COLUMN_ERROR_MESSAGE) val errorMessage: String = "") {

    companion object {
        const val COLUMN_ERROR_CODE = "errorCode"
        const val COLUMN_ERROR_MESSAGE = "errorMessage"
    }

    override fun toString(): String {
        return "BaseResponse(errorCode='$errorCode', errorMessage='$errorMessage')"
    }
}
