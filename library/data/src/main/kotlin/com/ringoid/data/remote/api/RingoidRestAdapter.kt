package com.ringoid.data.remote.api

import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.datainterface.remote.model.feed.ChatResponse
import com.ringoid.datainterface.remote.model.feed.FeedResponse
import com.ringoid.datainterface.remote.model.feed.LmmResponse
import com.ringoid.datainterface.remote.model.image.ImageUploadUrlResponse
import com.ringoid.datainterface.remote.model.image.UserImageListResponse
import com.ringoid.datainterface.remote.model.user.AuthCreateProfileResponse
import com.ringoid.datainterface.remote.model.user.UserSettingsResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface RingoidRestAdapter {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    @POST("auth/create_profile")
    fun createUserProfile(@Body body: RequestBody): Single<AuthCreateProfileResponse>

    @POST("auth/delete")
    fun deleteUserProfile(@Body body: RequestBody): Single<BaseResponse>

    @GET("auth/get_settings")
    fun getUserSettings(@Query("accessToken") accessToken: String): Single<UserSettingsResponse>

    @POST("auth/update_settings")
    fun updateUserSettings(@Body body: RequestBody): Single<BaseResponse>

    @POST("auth/update_profile")
    fun updateUserProfile(@Body body: RequestBody): Single<BaseResponse>

    // ------------------------------------------
    @POST("auth/claim")
    fun applyReferralCode(@Body body: RequestBody): Single<BaseResponse>

    /* Actions */
    // --------------------------------------------------------------------------------------------
    @POST("actions/actions")
    fun commitActions(@Body body: RequestBody): Single<CommitActionsResponse>

    /* Image */
    // --------------------------------------------------------------------------------------------
    @POST("image/get_presigned")
    fun getImageUploadUrl(@Body body: RequestBody): Single<ImageUploadUrlResponse>

    @GET("image/get_own_photos")
    fun getUserImages(@Query("accessToken") accessToken: String,
                      @Query("resolution") resolution: String): Single<UserImageListResponse>

    @POST("image/delete_photo")
    fun deleteUserImage(@Body body: RequestBody): Single<BaseResponse>

    @PUT
    fun uploadImage(@Url url: String, @Body body: RequestBody): Completable

    /* Feed */
    // --------------------------------------------------------------------------------------------
    @GET("feeds/chat")
    fun getChat(@Query("accessToken") accessToken: String,
                @Query("resolution") resolution: String?,
                @Query("userId") peerId: String?,
                @Query("lastActionTime") lastActionTime: Long = 0L): Single<ChatResponse>

    @POST("feeds/discover")
    fun getDiscover(@Body body: RequestBody): Single<FeedResponse>

    @Deprecated("LMM -> LC")
    @GET("feeds/get_new_faces")
    fun getNewFaces(@Query("accessToken") accessToken: String,
                    @Query("resolution") resolution: String?,
                    @Query("limit") limit: Int?,
                    @Query("lastActionTime") lastActionTime: Long = 0L): Single<FeedResponse>

    @Deprecated("LMM -> LC")
    @GET("feeds/get_lmm")
    fun getLmm(@Query("accessToken") accessToken: String,
               @Query("resolution") resolution: String?,
               @Query("source") source: String?,
               @Query("lastActionTime") lastActionTime: Long = 0L): Single<LmmResponse>

    @POST("feeds/get_lc")
    fun getLc(@Body body: RequestBody): Single<LmmResponse>

    /* Push */
    // --------------------------------------------------------------------------------------------
    @POST("push/update_fcm_token")
    fun updatePushToken(@Body body: RequestBody): Single<BaseResponse>

    /* Test */
    // --------------------------------------------------------------------------------------------
    @POST("timeout")
    fun debugTimeout(): Completable

    @POST("invalidtoken")
    fun debugInvalidToken(): Completable

    @POST("nonok")
    fun debugNotSuccess(): Completable

    @POST("auth/delete_for404")
    fun debugResponseWith404(): Completable

    @POST("old_version")
    fun debugOldVersion(): Completable

    @POST("internalerror")
    fun debugServerError(): Completable
}
