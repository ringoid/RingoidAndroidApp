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
import com.ringoid.domain.model.actions.OriginActionObject
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
import io.reactivex.functions.BiFunction
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
        fun reportSizeOfEssence(essence: CommitActionsEssence) {
            essence.actions.size.also { size -> "Committing $size action objects".let { DebugLogUtil.d(it) } }
                .takeIf { it >= ACTIONS_LIMIT_TO_WARN }
                ?.let { size ->
                    val extras = mutableListOf<Pair<String, String>>().apply {
                        add("size" to "$size")
                        addAll(essence.toContentString())
                    }
                    "Committing too many action objects at once".let { str ->
                        DebugLogUtil.w(str); Report.i(str, extras = extras)
                    }
                }
        }

        /**
         * Commits [essence] as a whole.
         */
        fun commitSingleChunk(essence: CommitActionsEssence): Single<Long> =
            cloud.commitActions(essence)
                .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)
                .map { it.lastActionTime }
                .onErrorResumeNext { e -> CommitActionsException(essence.actions, cause = e).let { Single.error<Long>(it) } }

        /**
         * Splits too large [essence] by chunks of fixed size and an optional tail of lesser size,
         * and then commits them in parallel. Delays possible errors and then combines all of them
         * into single error, which is then propagated downstream.
         */
        fun commitMultipleChunksParallel(essence: CommitActionsEssence): Single<Long> =
            essence.actions.chunked(ACTIONS_CHUNK_SIZE)
                .also { DebugLogUtil.d("Committing ${it.size} chunks by $ACTIONS_CHUNK_SIZE action objects") }
                .let { Observable.fromIterable(it) }
                .map { essence.copyWith(actions = it) }  // prepare chunk of action objects
                .flatMapSingle({ subEssence ->  // indices are not taken into account
                    // commit single chunk of action objects and handle error
                    cloud.commitActions(subEssence)
                        .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)
                        .onErrorResumeNext { Single.error(CommitActionsException(subEssence.actions, cause = it)) }
                }, true)
                .toList()
                .map { it.maxBy { it.lastActionTime } }
                .map { it.lastActionTime }
                .onErrorResumeNext {
                    when (it) {  // transform error
                        is CompositeException -> {
                            /**
                             * Given combined error, represented by [CompositeException], loop over it,
                             * extracting each particular error as instance of [CommitActionsException],
                             * then extract list of action objects that have failed to be committed from
                             * each one and combine all of them together in a single [CommitActionsException],
                             * that is then should be emitted downstream.
                             */
                            val failToCommit = it.exceptions
                                .filterIsInstance<CommitActionsException>()
                                .map { e -> e.failToCommit.toMutableList() }
                                .reduce { acc, collection -> acc.addAll(collection); acc }

                            // all action objects that have remained uncommitted are stored here
                            CommitActionsException(failToCommit, cause = it)
                        }
                        else -> it
                    }.let { e -> Single.error(e) }  // propagate error downstream
                }

        /**
         * Splits too large [essence] by chunks of fixed size and an optional tail of lesser size,
         * and then commits them sequentially one after another. Fails the whole chain if some chunk
         * is failed to be committed, and propagates a single error downstream.
         */
        fun commitMultipleChunksSequential(essence: CommitActionsEssence): Single<Long> {
            val chunks = essence.actions.chunked(ACTIONS_CHUNK_SIZE)
                .also { DebugLogUtil.d("Committing ${it.size} chunks by $ACTIONS_CHUNK_SIZE action objects") }
            val totalChunks = chunks.size

            return chunks
                .let { Observable.fromIterable(it) }
                .map { essence.copyWith(actions = it) }  // prepare chunk of action objects
                .zipWith(Observable.range(1, totalChunks),
                         BiFunction { subEssence: CommitActionsEssence, index: Int -> subEssence to index })
                .concatMapSingle { (subEssence, index) ->
                    // commit single chunk of action objects and handle error
                    cloud.commitActions(subEssence)
                        .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)
                        .onErrorResumeNext { Single.error(CommitActionsException(subEssence.actions, indexOfChunk = index, cause = it)) }
                }
                .toList()
                .map { it.maxBy { it.lastActionTime } }
                .map { it.lastActionTime }
                .onErrorResumeNext {
                    when (it) {
                        /**
                         * At some point committing of some chunk of action objects has failed,
                         * so the whole chain has stopped as well. Keep all yet uncommitted action
                         * objects in one single [CommitActionsException] and propagate it downstream.
                         */
                        is CommitActionsException -> {
                            val actions = mutableListOf<OriginActionObject>().apply {
                                addAll(it.failToCommit)
                                val from = it.indexOfChunk * ACTIONS_CHUNK_SIZE
                                if (from < essence.actions.size) {
                                    addAll(essence.actions.toMutableList().subList(from, essence.actions.size))
                                }
                            }
                            CommitActionsException(failToCommit = actions, cause = it)
                        }
                        else -> it
                    }.let { e -> Single.error(e) }  // propagate error downstream
                }
        }

        // --------------------------------------
        reportSizeOfEssence(essence)

        return if (essence.actions.size <= ACTIONS_CHUNK_SIZE) {
            commitSingleChunk(essence)  // commit actions all at once
        } else {
            commitMultipleChunksSequential(essence)  // split and commit by chunks
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
