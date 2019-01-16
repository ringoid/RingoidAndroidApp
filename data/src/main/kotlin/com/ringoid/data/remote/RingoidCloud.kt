package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssence
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingoidCloud @Inject constructor(private val restAdapter: RingoidRestAdapter) {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> =
        restAdapter.createUserProfile(essence.toBody())

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
    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse> =
        restAdapter.commitActions(essence.toBody())

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
        restAdapter.getImageUploadUrl(essence.toBody())

    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse> =
        restAdapter.getUserImages(accessToken = accessToken, resolution = resolution.resolution)

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
        restAdapter.deleteUserImage(essence.toBody())

    fun uploadImage(url: String, image: File): Completable {
        val body = RequestBody.create(MediaType.parse("image/*"), image)
        return restAdapter.uploadImage(url = url, body = body)
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L) =
        restAdapter.getNewFaces(accessToken = accessToken, resolution = resolution?.resolution, limit = limit, lastActionTime = lastActionTime)

    fun getLmm(accessToken: String, resolution: ImageResolution, lastActionTime: Long = 0L) =
        restAdapter.getLmm(accessToken = accessToken, resolution = resolution?.resolution, lastActionTime = lastActionTime)

    /* Test */
    // --------------------------------------------------------------------------------------------
}
