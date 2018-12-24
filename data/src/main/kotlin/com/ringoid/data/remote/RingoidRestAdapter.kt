package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.image.ImageListResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

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
    @POST("get_presigned")
    fun getImageUploadUrl(@Body body: RequestBody): Single<ImageUploadUrlResponse>

    @GET("get_own_photos")
    fun getUserImages(@Query("access_token") accessToken: String,
                      @Query("resolution") resolution: String): Single<ImageListResponse>

    @POST("delete_photo")
    fun deleteUserImage(@Body body: RequestBody): Single<BaseResponse>

    @PUT
    fun uploadImage(@Url url: String, @Body body: RequestBody): Completable

    /* Feed */
    // --------------------------------------------------------------------------------------------

    /* Test */
    // --------------------------------------------------------------------------------------------
}
