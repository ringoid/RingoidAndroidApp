package com.ringoid.datainterface.remote

import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.datainterface.remote.model.actions.CommitActionsResponse
import com.ringoid.datainterface.remote.model.feed.ChatResponse
import com.ringoid.datainterface.remote.model.feed.FeedResponse
import com.ringoid.datainterface.remote.model.feed.LmmResponse
import com.ringoid.datainterface.remote.model.image.ImageUploadUrlResponse
import com.ringoid.datainterface.remote.model.image.UserImageListResponse
import com.ringoid.datainterface.remote.model.user.AuthCreateProfileResponse
import com.ringoid.datainterface.remote.model.user.UserSettingsResponse
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.essence.push.PushTokenEssence
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.essence.user.ReferralCodeEssence
import com.ringoid.domain.model.essence.user.UpdateUserProfileEssence
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssence
import com.ringoid.domain.model.feed.Filters
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

interface IRingoidCloudFacade {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse>

    fun deleteUserProfile(accessToken: String): Single<BaseResponse>

    fun getUserSettings(accessToken: String): Single<UserSettingsResponse>

    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse>

    fun updateUserProfile(essence: UpdateUserProfileEssence): Single<BaseResponse>

    // ------------------------------------------
    fun applyReferralCode(essence: ReferralCodeEssence): Single<BaseResponse>

    /* Actions */
    // --------------------------------------------------------------------------------------------
    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse>

    /* Image */
    // --------------------------------------------------------------------------------------------
    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse>

    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse>

    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse>

    fun uploadImage(url: String, image: File): Completable

    /* Feed */
    // --------------------------------------------------------------------------------------------
    fun getChat(accessToken: String, resolution: ImageResolution, peerId: String, lastActionTime: Long = 0L): Single<ChatResponse>

    fun getDiscover(accessToken: String, resolution: ImageResolution, limit: Int?, filter: Filters?,
                    lastActionTime: Long = 0L): Single<FeedResponse>

    @Deprecated("LMM -> LC")
    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L): Single<FeedResponse>

    @Deprecated("LMM -> LC")
    fun getLmm(accessToken: String, resolution: ImageResolution, source: String?, lastActionTime: Long = 0L): Single<LmmResponse>

    fun getLc(accessToken: String, resolution: ImageResolution, limit: Int?, filter: Filters?,
              source: String?, lastActionTime: Long = 0L): Single<LmmResponse>

    /* Push */
    // --------------------------------------------------------------------------------------------
    fun updatePushToken(essence: PushTokenEssence): Single<BaseResponse>

    /* Test */
    // --------------------------------------------------------------------------------------------
    fun debugTimeout(): Completable
    fun debugInvalidToken(): Completable
    fun debugNotSuccess(): Completable
    fun debugResponseWith404(): Completable
    fun debugOldVersion(): Completable
    fun debugServerError(): Completable
}
