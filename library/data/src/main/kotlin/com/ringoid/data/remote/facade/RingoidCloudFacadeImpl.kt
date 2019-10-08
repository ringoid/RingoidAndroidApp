package com.ringoid.data.remote.facade

import com.ringoid.data.handleError
import com.ringoid.data.remote.api.RingoidCloud
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.datainterface.remote.model.feed.ChatResponse
import com.ringoid.datainterface.remote.model.feed.FeedResponse
import com.ringoid.datainterface.remote.model.feed.LmmResponse
import com.ringoid.datainterface.remote.model.image.ImageUploadUrlResponse
import com.ringoid.datainterface.remote.model.image.UserImageListResponse
import com.ringoid.datainterface.remote.model.user.AuthCreateProfileResponse
import com.ringoid.datainterface.remote.model.user.UserSettingsResponse
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.exception.CommitActionsException
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
import com.ringoid.report.log.Report
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingoidCloudFacadeImpl @Inject constructor(private val cloud: RingoidCloud) : IRingoidCloudFacade {

    companion object {
        private const val ACTIONS_CHUNK_SIZE = 40
        private const val ACTIONS_LIMIT_TO_WARN = 60
    }

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
    override fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> =
            cloud.createUserProfile(essence)

    override fun deleteUserProfile(accessToken: String): Single<BaseResponse> =
            cloud.deleteUserProfile(accessToken)

    override fun getUserSettings(accessToken: String): Single<UserSettingsResponse> =
            cloud.getUserSettings(accessToken)

    override fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse> =
            cloud.updateUserSettings(essence)

    override fun updateUserProfile(essence: UpdateUserProfileEssence): Single<BaseResponse> =
            cloud.updateUserProfile(essence)

    // ------------------------------------------
    override fun applyReferralCode(essence: ReferralCodeEssence): Single<BaseResponse> =
            cloud.applyReferralCode(essence)

    /* Actions */
    // --------------------------------------------------------------------------------------------
    override fun commitActions(essence: CommitActionsEssence): Single<Long> {
        essence.actions.size.also { size -> "Committing $size action objects".let { DebugLogUtil.d(it) } }
               .takeIf { it >= ACTIONS_LIMIT_TO_WARN }
               ?.let { size ->
                   val extras = mutableListOf<Pair<String, String>>().apply {
                       add("size" to "$size")
                       addAll(essence.toContentString())
                   }
                   Report.i("Committing too many action objects at once", extras = extras)
               }

        return if (essence.actions.size <= ACTIONS_CHUNK_SIZE) {  // commit actions all at once
            cloud.commitActions(essence)
                 .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)
                 .map { it.lastActionTime }
                 .onErrorResumeNext { e -> CommitActionsException(essence.actions, cause = e).let { Single.error<Long>(it) } }
        } else {  // split too large essence by chunks of fixed size and tail of lesser size
            essence.actions.chunked(ACTIONS_CHUNK_SIZE)
                .also { DebugLogUtil.d("Committing ${it.size} chunks by $ACTIONS_CHUNK_SIZE action objects") }
                .let { Observable.fromIterable(it) }
                .map { essence.copyWith(actions = it) }
                .flatMapSingle({ subEssence ->
                    cloud.commitActions(subEssence)  // commit single chunk of action objects and handle error
                         .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)
                         .onErrorResumeNext { Single.error(CommitActionsException(subEssence.actions, cause = it)) }
                }, true)
                .toList()
                .map { it.maxBy { it.lastActionTime } }
                .map { it.lastActionTime }
                .onErrorResumeNext { error ->
                    when (error) {
                        is CompositeException -> {
                            val failToCommit = error.exceptions
                                .filterIsInstance<CommitActionsException>()
                                .map { it.failToCommit.toMutableList() }
                                .reduce { acc, collection -> acc.addAll(collection); acc }

                            CommitActionsException(failToCommit, cause = error).let { Single.error<Long>(it) }
                        }
                        else -> Single.error<Long>(error)
                    }
                }
        }
    }

    /* Image */
    // --------------------------------------------------------------------------------------------
    override fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse> =
            cloud.getImageUploadUrl(essence)

    override fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse> =
            cloud.getUserImages(accessToken, resolution)

    override fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse> =
            cloud.deleteUserImage(essence)

    override fun uploadImage(url: String, image: File): Completable =
            cloud.uploadImage(url, image)

    /* Feed */
    // --------------------------------------------------------------------------------------------
    override fun getChat(accessToken: String, resolution: ImageResolution, peerId: String, lastActionTime: Long): Single<ChatResponse> =
        cloud.getChat(accessToken, resolution, peerId, lastActionTime)

    override fun getDiscover(accessToken: String, resolution: ImageResolution, limit: Int?,
                             filter: Filters?, lastActionTime: Long): Single<FeedResponse> =
        cloud.getDiscover(accessToken, resolution, limit, filter, lastActionTime)

    @Deprecated("LMM -> LC")
    override fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long): Single<FeedResponse> =
        cloud.getNewFaces(accessToken, resolution, limit, lastActionTime)

    @Deprecated("LMM -> LC")
    override fun getLmm(accessToken: String, resolution: ImageResolution, source: String?, lastActionTime: Long): Single<LmmResponse> =
        cloud.getLmm(accessToken, resolution, source, lastActionTime)

    override fun getLc(accessToken: String, resolution: ImageResolution, limit: Int?, filter: Filters?,
                       source: String?, lastActionTime: Long): Single<LmmResponse> =
        cloud.getLc(accessToken, resolution, limit, filter, source, lastActionTime)

    /* Push */
    // --------------------------------------------------------------------------------------------
    override fun updatePushToken(essence: PushTokenEssence): Single<BaseResponse> =
        cloud.updatePushToken(essence)

    /* Test */
    // --------------------------------------------------------------------------------------------
    override fun debugTimeout(): Completable = cloud.debugTimeout()
    override fun debugInvalidToken(): Completable = cloud.debugInvalidToken()
    override fun debugNotSuccess(): Completable = cloud.debugNotSuccess()
    override fun debugResponseWith404(): Completable = cloud.debugResponseWith404()
    override fun debugOldVersion(): Completable = cloud.debugOldVersion()
    override fun debugServerError(): Completable = cloud.debugServerError()
}
