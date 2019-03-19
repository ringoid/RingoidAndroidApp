package com.ringoid.data.remote

import com.ringoid.data.remote.debug.keepDataForDebug
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.log.breadcrumb
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
class RingoidCloud @Inject constructor(private val restAdapter: RingoidRestAdapter, private val cloudDebug: ICloudDebug) {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> =
        restAdapter.createUserProfile(essence.toBody())
            .breadcrumb("createUserProfile", essence.toSentryData())
            .logRequest("createUserProfile")
            .logResponse("createUserProfile")

    fun deleteUserProfile(accessToken: String): Single<BaseResponse> {
        val content = "{\"accessToken\":\"$accessToken\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.deleteUserProfile(body)
            .breadcrumb("deleteUserProfile", "accessToken" to "")
            .logRequest("deleteUserProfile")
            .logResponse("deleteUserProfile")
    }

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
        restAdapter.getUserSettings(accessToken = accessToken)
            .breadcrumb("getUserSettings", "accessToken" to "")
            .logRequest("getUserSettings")
            .logResponse("getUserSettings")

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> =
        restAdapter.updateUserSettings(essence.toBody())
            .breadcrumb("updateUserSettings", essence.toSentryData())
            .logRequest("updateUserSettings")
            .logResponse("updateUserSettings")

    /* Actions */
    // --------------------------------------------------------------------------------------------
    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse> =
        restAdapter.commitActions(essence.toBody())
            .breadcrumb("commitActions", essence.toSentryData())
            .logRequest("commitActions", essence.toDebugData())
            .logResponse("commitActions", essence.toDebugData())

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
        restAdapter.getImageUploadUrl(essence.toBody())
            .breadcrumb("getImageUploadUrl", essence.toSentryData())
            .logRequest("getImageUploadUrl")
            .logResponse("getImageUploadUrl")

    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse> =
        restAdapter.getUserImages(accessToken = accessToken, resolution = resolution.resolution)
            .keepDataForDebug(cloudDebug,"request" to "getUserImages", "resolution" to "$resolution")
            .breadcrumb("getUserImages", "accessToken" to "", "resolution" to "$resolution")
            .logRequest("getUserImages")
            .logResponse("UserPhotos")

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
        restAdapter.deleteUserImage(essence.toBody())
            .breadcrumb("deleteUserImage", essence.toSentryData())
            .logRequest("deleteUserImage")
            .logResponse("deleteUserImage")

    fun uploadImage(url: String, image: File): Completable {
        val body = RequestBody.create(MediaType.parse("image/*"), image)
        return restAdapter.uploadImage(url = url, body = body)
            .breadcrumb("uploadImage", "url" to url)
            .logRequest("uploadImage")
            .logResponse("uploadImage")
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L) =
        restAdapter.getNewFaces(accessToken = accessToken, resolution = resolution.resolution, limit = limit, lastActionTime = lastActionTime)
            .keepDataForDebug(cloudDebug, "request" to "getNewFaces", "resolution" to "$resolution")
            .breadcrumb("getNewFaces", "accessToken" to "", "resolution" to "$resolution",
                        "lastActionTime" to "$lastActionTime")
            .logRequest("getNewFaces", "lastActionTime" to "$lastActionTime")
            .logResponse("NewFaces")

    fun getLmm(accessToken: String, resolution: ImageResolution, source: String?, lastActionTime: Long = 0L) =
        restAdapter.getLmm(accessToken = accessToken, resolution = resolution.resolution, source = source, lastActionTime = lastActionTime)
            .keepDataForDebug(cloudDebug, "request" to "getLmm", "resolution" to "$resolution")
            .breadcrumb("getLmm", "accessToken" to "",
                        "resolution" to "$resolution", "source" to "$source", "lastActionTime" to "$lastActionTime")
            .logRequest("getLmm", "lastActionTime" to "$lastActionTime")
            .logResponse("LMM")

    /* Test */
    // --------------------------------------------------------------------------------------------
    fun debugTimeout(): Completable = restAdapter.debugTimeout().logResponse()
    fun debugInvalidToken(): Completable = restAdapter.debugInvalidToken().logResponse()
    fun debugNotSuccess(): Completable = restAdapter.debugNotSuccess().logResponse()
    fun debugOldVersion(): Completable = restAdapter.debugOldVersion().logResponse()
    fun debugServerError(): Completable = restAdapter.debugServerError().logResponse()
}
