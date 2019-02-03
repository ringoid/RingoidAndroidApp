package com.ringoid.data.remote.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.user.CurrentUser

/**
 * {
 *   "accessToken":"adasdasd-fadfs-sdffd",
 *   "customerId":"ksjdhfha-asff",
 *   "errorCode":"",
 *   "errorMessage":""
 * }
 */
class AuthCreateProfileResponse(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String = "",
    @Expose @SerializedName(COLUMN_USER_ID) val userId: String = "",
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec), Mappable<CurrentUser> {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_USER_ID = "customerId"
    }

    override fun map(): CurrentUser = CurrentUser(id = userId, accessToken = accessToken)
}
