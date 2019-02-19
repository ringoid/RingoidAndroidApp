package com.ringoid.data.remote

import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.domain.breadcrumb
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
            .breadcrumb("createUserProfile", essence.toSentryData())
            .logResponse("createUserProfile")
            .checkResponseTime()

    fun deleteUserProfile(accessToken: String): Single<BaseResponse> {
        val content = "{\"accessToken\":\"$accessToken\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.deleteUserProfile(body)
            .breadcrumb("deleteUserProfile", "accessToken" to "")
            .logResponse("deleteUserProfile")
            .checkResponseTime()
    }

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
        restAdapter.getUserSettings(accessToken = accessToken)
            .breadcrumb("getUserSettings", "accessToken" to "")
            .logResponse("getUserSettings")
            .checkResponseTime()

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> =
        restAdapter.updateUserSettings(essence.toBody())
            .breadcrumb("updateUserSettings", essence.toSentryData())
            .logResponse("updateUserSettings")
            .checkResponseTime()

    /* Actions */
    // --------------------------------------------------------------------------------------------
    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse> =
        restAdapter.commitActions(essence.toBody())
            .breadcrumb("commitActions", essence.toSentryData())
            .logResponse("commitActions")
            .checkResponseTime()

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
        restAdapter.getImageUploadUrl(essence.toBody())
            .breadcrumb("getImageUploadUrl", essence.toSentryData())
            .logResponse("getImageUploadUrl")
            .checkResponseTime()

    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse> =
        restAdapter.getUserImages(accessToken = accessToken, resolution = resolution.resolution)
            .breadcrumb("getUserImages", "accessToken" to "", "resolution" to "$resolution")
            .logResponse("getUserImages")
            .checkResponseTime("UserPhotos")

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
        restAdapter.deleteUserImage(essence.toBody())
            .breadcrumb("deleteUserImage", essence.toSentryData())
            .logResponse("deleteUserImage")
            .checkResponseTime()

    fun uploadImage(url: String, image: File): Completable {
        val body = RequestBody.create(MediaType.parse("image/*"), image)
        return restAdapter.uploadImage(url = url, body = body)
            .breadcrumb("uploadImage", "url" to url)
            .logResponse("uploadImage")
            .checkResponseTime()
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L) =
        restAdapter.getNewFaces(accessToken = accessToken, resolution = resolution.resolution, limit = limit, lastActionTime = lastActionTime)
            .breadcrumb("getNewFaces", "accessToken" to "", "resolution" to "$resolution",
                        "lastActionTime" to "$lastActionTime")
            .logResponse("getNewFaces")
            .checkResponseTime("NewFaces")

    fun getLmm(accessToken: String, resolution: ImageResolution, lastActionTime: Long = 0L) =
        restAdapter.getLmm(accessToken = accessToken, resolution = resolution.resolution, lastActionTime = lastActionTime)
            .breadcrumb("getLmm", "accessToken" to "",
                        "resolution" to "$resolution", "lastActionTime" to "$lastActionTime")
            .logResponse("getLmm")
            .checkResponseTime("LMM")

    /* Test */
    // --------------------------------------------------------------------------------------------
    fun debugTimeout(): Completable = restAdapter.debugTimeout().checkResponseTime()
    fun debugInvalidToken(): Completable = restAdapter.debugInvalidToken().checkResponseTime()
    fun debugNotSuccess(): Completable = restAdapter.debugNotSuccess().checkResponseTime()
    fun debugOldVersion(): Completable = restAdapter.debugOldVersion().checkResponseTime()
    fun debugServerError(): Completable = restAdapter.debugServerError().checkResponseTime()
}
