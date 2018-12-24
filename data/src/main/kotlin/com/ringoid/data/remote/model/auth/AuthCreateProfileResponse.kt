package com.ringoid.data.remote.model.auth

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "customerId":"ksjdhfha-asff",
 *   "errorCode":"",
 *   "errorMessage":""
 * }
 */
class AuthCreateProfileResponse(
    @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String = "",
    @SerializedName(COLUMN_CUSTOMER_ID) val customerId: String = "",
    errorCode: String = "", errorMessage: String = "") : BaseResponse(errorCode, errorMessage) {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_CUSTOMER_ID = "customerId"
    }
}
