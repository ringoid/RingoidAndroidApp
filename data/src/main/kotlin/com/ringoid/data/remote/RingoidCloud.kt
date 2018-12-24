package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.action.CommitActionsEssence
import com.ringoid.data.remote.model.image.ImageListResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.essence.ImageDeleteEssence
import com.ringoid.data.remote.model.image.essence.ImageUploadUrlEssence
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.data.remote.model.user.essence.AuthCreateProfileEssence
import com.ringoid.data.remote.model.user.essence.UpdateUserSettingsEssence
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import javax.inject.Singleton

@Singleton
class RingoidCloud(private val restAdapter: RingoidRestAdapter) {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> =restAdapter.createUserProfile(essence.toBody())

    fun deleteUserProfile(accessToken: String): Single<BaseResponse> {
        val content = "{\"accessToken\":\"$accessToken\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.deleteUserProfile(body)
    }

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
        restAdapter.getUserSettings(accessToken = accessToken)

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> =
        restAdapter.updateUserSettings(essence.toBody())

    /* Actions */
    // --------------------------------------------------------------------------------------------
    fun commitActions(essence: CommitActionsEssence): Single<BaseResponse> =
            restAdapter.commitActions(essence.toBody())

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
            restAdapter.getImageUploadUrl(essence.toBody())

    fun getUserImages(accessToken: String, resolution: String): Single<ImageListResponse> =
            restAdapter.getUserImages(accessToken = accessToken, resolution = resolution)

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
        restAdapter.deleteUserImage(essence.toBody())

    fun uploadImage(url: String, image: File): Completable {
        val body = RequestBody.create(MediaType.parse("image/*"), image)
        return restAdapter.uploadImage(url = url, body = body)
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getNewFaces(accessToken: String, resolution: String, limit: Int) =
            restAdapter.getNewFaces(accessToken = accessToken, resolution = resolution, limit = limit)

    fun getLmm(accessToken: String, resolution: String, lastActionTime: Long) =
            restAdapter.getLmm(accessToken = accessToken, resolution = resolution, lastActionTime = lastActionTime)

    /* Test */
    // --------------------------------------------------------------------------------------------
}
