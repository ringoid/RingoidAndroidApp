package com.ringoid.data.remote

import com.google.firebase.perf.FirebasePerformance
import com.ringoid.data.remote.debug.keepDataForDebug
import com.ringoid.data.remote.debug.keepResultForDebug
import com.ringoid.data.remote.model.BaseResponse
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.remote.model.feed.LmmResponse
import com.ringoid.data.remote.model.image.ImageUploadUrlResponse
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.data.remote.model.user.AuthCreateProfileResponse
import com.ringoid.data.remote.model.user.UserSettingsResponse
import com.ringoid.domain.debug.ICloudDebug
import com.ringoid.domain.log.breadcrumb
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.model.essence.feed.FilterEssence
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.essence.push.PushTokenEssence
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.essence.user.ReferralCodeEssence
import com.ringoid.domain.model.essence.user.UpdateUserProfileEssence
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
            .keepDataForDebug(cloudDebug, "request" to "createUserProfile")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("createUserProfile", essence.toSentryData(), "yearOfBirth" to "${essence.yearOfBirth}", "gender" to essence.sex)
            .logRequest("createUserProfile")
            .logResponse("createUserProfile")

    fun deleteUserProfile(accessToken: String): Single<BaseResponse> {
        val content = "{\"accessToken\":\"$accessToken\"}"
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.deleteUserProfile(body)
            .keepDataForDebug(cloudDebug, "request" to "deleteUserProfile")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("deleteUserProfile", "accessToken" to "")
            .logRequest("deleteUserProfile")
            .logResponse("deleteUserProfile")
    }

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
        restAdapter.getUserSettings(accessToken = accessToken)
            .keepDataForDebug(cloudDebug, "request" to "getUserSettings")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getUserSettings", "accessToken" to "")
            .logRequest("getUserSettings")
            .logResponse("getUserSettings")

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> =
        restAdapter.updateUserSettings(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "updateUserSettings")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("updateUserSettings", essence.toSentryData())
            .logRequest("updateUserSettings", essence.toDebugData())
            .logResponse("updateUserSettings")

    fun updateUserProfile(essence: UpdateUserProfileEssence): Single<BaseResponse> =
        restAdapter.updateUserProfile(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "updateUserProfile")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("updateUserProfile", essence.toSentryData())
            .logRequest("updateUserProfile", essence.toDebugData())
            .logResponse("updateUserProfile")

    // ------------------------------------------
    fun applyReferralCode(essence: ReferralCodeEssence): Single<BaseResponse> =
        restAdapter.applyReferralCode(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "applyReferralCode")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("applyReferralCode", essence.toSentryData())
            .logRequest("applyReferralCode", essence.toDebugData())
            .logResponse("applyReferralCode", essence.toDebugData())

    /* Actions */
    // --------------------------------------------------------------------------------------------
    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse> =
        restAdapter.commitActions(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "commitActions")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("commitActions", essence.toSentryData())
            .logRequest("commitActions", essence.toDebugData())
            .logResponse("commitActions", essence.toDebugData())

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
        restAdapter.getImageUploadUrl(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "getImageUploadUrl")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getImageUploadUrl", essence.toSentryData())
            .logRequest("getImageUploadUrl")
            .logResponse("getImageUploadUrl")

    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse> =
        restAdapter.getUserImages(accessToken = accessToken, resolution = resolution.resolution)
            .keepDataForDebug(cloudDebug,"request" to "getUserImages", "resolution" to "$resolution")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getUserImages", "accessToken" to "", "resolution" to "$resolution")
            .logRequest("getUserImages")
            .logResponse("UserPhotos")

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
        restAdapter.deleteUserImage(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "deleteUserImage")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("deleteUserImage", essence.toSentryData())
            .logRequest("deleteUserImage")
            .logResponse("deleteUserImage")

    fun uploadImage(url: String, image: File): Completable {
        val body = RequestBody.create(MediaType.parse("image/*"), image)
        val trace = FirebasePerformance.getInstance().newTrace("image upload")
        return restAdapter.uploadImage(url = url, body = body)
            .keepDataForDebug(cloudDebug, "request" to "uploadImage")
            .breadcrumb("uploadImage", "url" to url)
            .logRequest("uploadImage")
            .logResponse("uploadImage")
            .doOnSubscribe { trace.start() }
            .doFinally { trace.stop() }
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getChat(accessToken: String, resolution: ImageResolution, peerId: String, lastActionTime: Long = 0L) =
        restAdapter.getChat(accessToken = accessToken, resolution = resolution.resolution, peerId = peerId, lastActionTime = lastActionTime)
            .keepDataForDebug(cloudDebug, "request" to "getChat", "resolution" to "$resolution", "peerId" to peerId, "lastActionTime" to "$lastActionTime")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getChat", "accessToken" to "", "resolution" to "$resolution", "peerId" to peerId,
                        "lastActionTime" to "$lastActionTime")
            .logRequest("getChat", "lastActionTime" to "$lastActionTime", "peerId" to peerId)
            .logResponse("Chat")

    @Deprecated("LMM -> LC")
    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L) =
        restAdapter.getNewFaces(accessToken = accessToken, resolution = resolution.resolution, limit = limit, lastActionTime = lastActionTime)
            .keepDataForDebug(cloudDebug, "request" to "getNewFaces", "resolution" to "$resolution", "lastActionTime" to "$lastActionTime")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getNewFaces", "accessToken" to "", "resolution" to "$resolution",
                        "lastActionTime" to "$lastActionTime")
            .logRequest("getNewFaces", "lastActionTime" to "$lastActionTime")
            .logResponse("NewFaces")

    @Deprecated("LMM -> LC")
    fun getLmm(accessToken: String, resolution: ImageResolution, source: String?, lastActionTime: Long = 0L) =
        restAdapter.getLmm(accessToken = accessToken, resolution = resolution.resolution, source = source, lastActionTime = lastActionTime)
            .keepDataForDebug(cloudDebug, "request" to "getLmm", "resolution" to "$resolution", "lastActionTime" to "$lastActionTime")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getLmm", "accessToken" to "",
                        "resolution" to "$resolution", "source" to "$source", "lastActionTime" to "$lastActionTime")
            .logRequest("getLmm", "lastActionTime" to "$lastActionTime")
            .logResponse("LMM")

    fun getLc(accessToken: String, resolution: ImageResolution, limit: Int?, filter: FilterEssence?,
              source: String?, lastActionTime: Long = 0L): Single<LmmResponse> {
        val contentList = mutableListOf<String>().apply {
            add("\"accessToken\":\"$accessToken\"")
            add("\"resolution\":\"$resolution\"")
            add("\"lastActionTime\":$lastActionTime")
            filter?.let { add("\"filter\":${filter.toJson()}") }
            limit?.takeIf { it > 0 }?.let { add("\"limit\":$limit") }
            source?.let { add("\"source\":$source") }
        }
        val content = contentList.joinToString(",", "{", "}")
        val body = RequestBody.create(MediaType.parse("application/json"), content)
        return restAdapter.getLc(body)
            .keepDataForDebug(cloudDebug, "request" to "getLc", "resolution" to "$resolution", "lastActionTime" to "$lastActionTime")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("getLc", "accessToken" to "",
                        "resolution" to "$resolution", "source" to "$source", "lastActionTime" to "$lastActionTime")
            .logRequest("getLmm", "lastActionTime" to "$lastActionTime")
            .logResponse("LC")
    }


    /* Push */
    // --------------------------------------------------------------------------------------------
    fun updatePushToken(essence: PushTokenEssence): Single<BaseResponse> =
        restAdapter.updatePushToken(essence.toBody())
            .keepDataForDebug(cloudDebug, "request" to "updatePushToken")
            .keepResultForDebug(cloudDebug)
            .breadcrumb("updatePushToken", essence.toSentryData())
            .logRequest("updatePushToken")
            .logResponse("updatePushToken")

    /* Test */
    // --------------------------------------------------------------------------------------------
    fun debugTimeout(): Completable = restAdapter.debugTimeout().logResponse()
    fun debugInvalidToken(): Completable = restAdapter.debugInvalidToken().logResponse()
    fun debugNotSuccess(): Completable = restAdapter.debugNotSuccess().logResponse()
    fun debugResponseWith404(): Completable = restAdapter.debugResponseWith404().logResponse()
    fun debugOldVersion(): Completable = restAdapter.debugOldVersion().logResponse()
    fun debugServerError(): Completable = restAdapter.debugServerError().logResponse()
}
