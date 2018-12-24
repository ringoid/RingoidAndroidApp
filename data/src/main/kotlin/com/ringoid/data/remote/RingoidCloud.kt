package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.data.remote.model.user.essence.AuthCreateProfileEssence
import com.ringoid.data.remote.model.user.essence.UpdateUserSettingsEssence
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody

class RingoidCloud(private val restAdapter: RingoidRestAdapter) {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> {
        val body = RequestBody.create(MediaType.parse("application/json"), essence.toJson())
        return restAdapter.createUserProfile(body)
    }

    fun deleteUserProfile(accessToken: String): Single<BaseResponse> {
        val content = "{\"accessToken\":\"$accessToken\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.deleteUserProfile(body)
    }

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
        restAdapter.getUserSettings(accessToken = accessToken)

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> {
        val body = RequestBody.create(MediaType.parse("application/json"), essence.toJson())
        return restAdapter.updateUserSettings(body)
    }

    /* Actions */
    // --------------------------------------------------------------------------------------------

    /* Image */
    // --------------------------------------------------------------------------------------------

    /* Feed */
    // --------------------------------------------------------------------------------------------

    /* Test */
    // --------------------------------------------------------------------------------------------
}
