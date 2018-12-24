package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RingoidRestAdapter {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    @POST("create_profile")
    fun createUserProfile(@Body body: RequestBody): Single<AuthCreateProfileResponse>

    @POST("delete")
    fun deleteUserProfile(@Body body: RequestBody): Single<BaseResponse>

    @GET("get_settings")
    fun getUserSettings(@Query("access_token") accessToken: String): Single<UserSettingsResponse>

    @POST("update_settings")
    fun updateUserSettings(@Body body: RequestBody): Single<BaseResponse>

    /* Actions */
    // --------------------------------------------------------------------------------------------

    /* Image */
    // --------------------------------------------------------------------------------------------

    /* Feed */
    // --------------------------------------------------------------------------------------------

    /* Test */
    // --------------------------------------------------------------------------------------------
}
