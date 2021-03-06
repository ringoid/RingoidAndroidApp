package com.ringoid.repository.debug

import com.ringoid.data.handleError
import com.ringoid.data.handleErrorNoRetry
import com.ringoid.data.local.shared_prefs.accessCompletable
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.DaggerCloudComponent
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.datainterface.di.PerAlreadySeen
import com.ringoid.datainterface.di.PerBlock
import com.ringoid.datainterface.local.user.IUserFeedDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.domain.model.feed.EmptyFeed
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.NoFilters
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.repository.debug.IDebugRepository
import com.ringoid.report.exception.SimulatedException
import com.ringoid.report.log.breadcrumb
import com.ringoid.repository.BaseRepository
import com.ringoid.utility.DebugOnly
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton @DebugOnly
class DebugRepository @Inject constructor(
    @PerAlreadySeen private val alreadySeenProfilesCache: IUserFeedDbFacade,
    @PerBlock private val blockedProfilesCache: IUserFeedDbFacade,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IDebugRepository {

    companion object {
        internal fun getFeed(page: Int): Feed = when (page) {
            0 -> Feed(profiles = listOf(
                Profile(id = "2bd92a2880820449502181b45687aad6e90a6132", images = listOf(Image(id = "1440x1920_53d4adfa15f0e0a1d12443a35c49075c6e501373", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/53d4adfa15f0e0a1d12443a35c49075c6e501373_1440x1920.jpg"))),
                Profile(id = "2bd92a2880820449502181b45687aad6e90a6133", images = listOf(Image(id = "1440x1920_ef1433e1debd5a641302cea5e18efb5cfcb66548", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/ef1433e1debd5a641302cea5e18efb5cfcb66548_1440x1920.jpg"))),
                Profile(id = "2bd92a2880820449502181b45687aad6e90a6134", images = listOf(Image(id = "1440x1920_6d3fc4aecb8fad73a0f448ef69df6d5d872663b2", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/6d3fc4aecb8fad73a0f448ef69df6d5d872663b2_1440x1920.jpg"))),
                Profile(id = "2bd92a2880820449502181b45687aad6e90a6135", images = listOf(Image(id = "1440x1920_a5a90e52adade8b11559993c83441c755229bcf3", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/a5a90e52adade8b11559993c83441c755229bcf3_1440x1920.jpg")))))
            1 -> Feed(profiles = listOf(
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c3", images = listOf(Image(id = "1440x1920_7eee6dbfa91140de1445c44555067bb3330cb0a3", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/7eee6dbfa91140de1445c44555067bb3330cb0a3_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c4", images = listOf(Image(id = "1440x1920_b5fa4f37e515a208d3c8d8ce0675034ca332899e", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/b5fa4f37e515a208d3c8d8ce0675034ca332899e_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c5", images = listOf(Image(id = "1440x1920_1b38317d0ab13fe429b9d316bd362fa9b308c12f", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/1b38317d0ab13fe429b9d316bd362fa9b308c12f_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c6", images = listOf(Image(id = "1440x1920_80015208dcc4ed87fa92f861c09350c90c62bca2", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/80015208dcc4ed87fa92f861c09350c90c62bca2_1440x1920.jpg")))))
            2 -> Feed(profiles = listOf(
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7342", images = listOf(Image(id = "1440x1920_2ec1de5fbc17a0f8a4cddea99ba5e781fb4ca342", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/2ec1de5fbc17a0f8a4cddea99ba5e781fb4ca342_1440x1920.jpg"))),
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7343", images = listOf(Image(id = "1440x1920_70149f01fb2651ea65753f4fd83792de789122c9", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/70149f01fb2651ea65753f4fd83792de789122c9_1440x1920.jpg"))),
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7344", images = listOf(Image(id = "1440x1920_3397e2a9f74cd9ae2cd6f0a84b2cc6c1e5ee59bc", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/3397e2a9f74cd9ae2cd6f0a84b2cc6c1e5ee59bc_1440x1920.jpg"))),
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7345", images = listOf(Image(id = "1440x1920_1fa4ad0f0a0b4e4d505133cb88691cfe922bf108", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/1fa4ad0f0a0b4e4d505133cb88691cfe922bf108_1440x1920.jpg")))))
            3 -> Feed(profiles = listOf(
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7343", images = listOf(Image(id = "1440x1920_70149f01fb2651ea65753f4fd83792de789122c9", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/70149f01fb2651ea65753f4fd83792de789122c9_1440x1920.jpg"))),
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7344", images = listOf(Image(id = "1440x1920_3397e2a9f74cd9ae2cd6f0a84b2cc6c1e5ee59bc", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/3397e2a9f74cd9ae2cd6f0a84b2cc6c1e5ee59bc_1440x1920.jpg"))),
                Profile(id = "5135f8abaa35400140c5e8ed14b2e65fd1e451e8", images = listOf(Image(id = "1440x1920_b9609534ffb499f8bad7cf553b120dc502d189d3", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/b9609534ffb499f8bad7cf553b120dc502d189d3_1440x1920.jpg"))),  // new one
                Profile(id = "30b49531684f7de2685b044d13e1c94992ad7345", images = listOf(Image(id = "1440x1920_1fa4ad0f0a0b4e4d505133cb88691cfe922bf108", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/1fa4ad0f0a0b4e4d505133cb88691cfe922bf108_1440x1920.jpg")))))
            4 -> Feed(profiles = listOf(  // already seen on page '1'
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c3", images = listOf(Image(id = "1440x1920_7eee6dbfa91140de1445c44555067bb3330cb0a3", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/7eee6dbfa91140de1445c44555067bb3330cb0a3_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c4", images = listOf(Image(id = "1440x1920_b5fa4f37e515a208d3c8d8ce0675034ca332899e", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/b5fa4f37e515a208d3c8d8ce0675034ca332899e_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c5", images = listOf(Image(id = "1440x1920_1b38317d0ab13fe429b9d316bd362fa9b308c12f", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/1b38317d0ab13fe429b9d316bd362fa9b308c12f_1440x1920.jpg"))),
                Profile(id = "f51a19a345f0ffa691ae30d41275ef81fb1343c6", images = listOf(Image(id = "1440x1920_80015208dcc4ed87fa92f861c09350c90c62bca2", uri = "https://s3-eu-west-1.amazonaws.com/stage-ringoid-public-photo/80015208dcc4ed87fa92f861c09350c90c62bca2_1440x1920.jpg")))))
            else -> EmptyFeed
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun commitActionObjectsWithFailAllRetries(): Completable =
        aObjPool.triggerSource().ignoreElement()

    // --------------------------------------------------------------------------------------------
    private var requestAttempt: Int = 0
    private var requestRepeatAfterDelayAttempt: Int = 0

    private fun getAndIncrementRequestAttempt(): Int = requestAttempt++
    private fun getAndIncrementRequestRepeatAfterDelayAttempt(): Int = requestRepeatAfterDelayAttempt++

    override fun requestWithFailAllRetries(): Completable =
        Single.just(BaseResponse(errorCode = "DebugError", errorMessage = "Debug error"))
            .breadcrumb("requestWithFailAllRetries", "debug" to "debug")
            .handleError(count = 3)
            .ignoreElement()  // convert to Completable

    override fun requestWithFailNTimesBeforeSuccess(count: Int): Completable =
        Single.just(0L)
            .flatMap {
                val i = getAndIncrementRequestAttempt()
                if (i < count) Single.just(BaseResponse(errorCode = "DebugError", errorMessage = "Debug error"))
                else Single.just(BaseResponse())
            }
            .handleError(count = count * 2, delay = 250L)
            .doFinally { requestAttempt = 0 }
            .ignoreElement()  // convert to Completable

    override fun requestWithRepeatAfterDelay(delay: Long): Completable =
        Single.just(0L)
            .flatMap {
                val i = getAndIncrementRequestRepeatAfterDelayAttempt()
                Single.just(BaseResponse(repeatRequestAfter = if (i < 1) delay else 0))
            }
            .handleError()
            .doFinally { requestRepeatAfterDelayAttempt = 0 }
            .ignoreElement()  // convert to Completable

    // ------------------------------------------
    override fun requestWithInvalidAccessToken(token: String): Completable =
        cloud.getUserImages(accessToken = token, resolution = ImageResolution._480x640)
            .handleError(count = 2)
            .ignoreElement()  // convert to Completable

    override fun requestWithUnsupportedAppVersion(): Completable {
        val cloud = DaggerCloudComponent.builder()
            .cloudModule(CloudModule(BuildConfig.BUILD_NUMBER / 2))
            .ringoidCloudModule(RingoidCloudModule())
            .build()
            .cloud()

        return spm.accessCompletable {
            cloud.getDiscover(it.accessToken, ImageResolution._480x640, 20, NoFilters, lastActionTime = aObjPool.lastActionTime())
                .handleErrorNoRetry()
                .map { it.map() }
                .ignoreElement()  // convert to Completable
        }
    }

    override fun requestWithWrongParameters(): Completable {
        val essence = AuthCreateProfileEssence(yearOfBirth = 1930, sex = "shemale")
        return cloud.createUserProfile(essence)
            .handleError()
            .ignoreElement()  // convert to Completable
    }

    // ------------------------------------------
    override fun debugRequestTimeOut(): Completable = cloud.debugTimeout()
    override fun debugNotSuccessResponse(): Completable = cloud.debugNotSuccess()
    override fun debugResponseWith404(): Completable = cloud.debugResponseWith404()
    override fun debugRequestCausingServerError(): Completable = cloud.debugServerError()
    override fun debugRequestWithInvalidAccessToken(): Completable = cloud.debugInvalidToken()
    override fun debugRequestWithUnsupportedAppVersion(): Completable = cloud.debugOldVersion()

    // ------------------------------------------
    /**
     * Doublestream subscribe on debug
     * Upstream debug response: $it
     * Upstream debug mapping 0
     * Downstream debug mapping 0
     *     Internal upstream subscribe debug
     *     Internal upstream debug response: $it
     *     Internal upstream debug mapping 0
     * Upstream debug mapping 1
     * ERROR -> RETRY
     *
     * ... retry
     *
     * Doublestream subscribe on debug
     * Upstream debug response: $it
     * Upstream debug mapping 0
     * Downstream debug mapping 0
     *     Internal upstream subscribe debug
     *     Internal upstream debug response: $it
     *     Internal upstream debug mapping 0
     * Upstream debug mapping 1
     *
     * Downstream failed all debug retries: $it
     */
    override fun debugHandleErrorDoublestream(): Completable =
        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { Timber.i("Doublestream subscribe on debug") }
            .doOnSuccess { Timber.v("Upstream debug response: $it") }
            .doOnError { Timber.v("Upstream failed al debug retries: $it") }
            .map { Timber.v("Upstream debug mapping 0"); it }
            .handleError(count = 4)
            .map { Timber.d("Downstream debug mapping 0"); it }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { Timber.v("Internal upstream subscribe debug") }
                    .doOnSuccess { Timber.v("Internal upstream debug response: $it") }
                    .doOnError { Timber.v("Internal upstream failed all retries: $it") }
                    .map { Timber.v("Internal upstream debug mapping 0"); it }
            }
            .map { Timber.v("Upstream debug mapping 1"); it }
            .handleError(count = 4)
            .map { Timber.d("Downstream debug mapping 1"); it }
            .doOnSuccess { Timber.d("Downstream debug response: $it") }
            .doOnError { Timber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()

    /**
     * Downstream subscribe on debug
     * Upstream debug response: $it
     *     Internal subscribe debug
     *     Internal debug response: $it
     *     Internal upstream debug mapping 1
     * Upstream debug mapping 1
     * ERROR -> RETRY
     *
     * ... retry
     *
     * Downstream subscribe on debug
     * Upstream debug response: $it
     *     Internal subscribe debug
     *     Internal debug response: $it
     *     Internal upstream debug mapping 1
     * Upstream debug mapping 1
     * ERROR -> RETRY
     *
     * Downstream failed all debug retries: $it
     */
    override fun debugHandleErrorDownstream(): Completable =
        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { Timber.i("Downstream subscribe on debug") }
            .doOnSuccess { Timber.v("Upstream debug response: $it") }
            .doOnError { Timber.v("Upstream failed al debug retries: $it") }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { Timber.v("Internal subscribe debug") }
                    .doOnSuccess { Timber.v("Internal debug response: $it") }
                    .doOnError { Timber.v("Internal failed all retries: $it") }
                    .map { Timber.v("Internal upstream debug mapping 1"); it }
            }
            .map { Timber.v("Upstream debug mapping 1"); it }
            .handleError(count = 4)
            .map { Timber.d("Downstream debug mapping 1"); it }
            .doOnSuccess { Timber.d("Downstream debug response: $it") }
            .doOnError { Timber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()

    /**
     * Multistream subscribe on debug
     * Upstream debug response: $it
     * Upstream debug mapping 0
     * Downstream debug mapping 0
     *     Internal upstream subscribe debug
     *     Internal upstream debug response: $it
     *     Internal upstream debug mapping 0
     *     ERROR -> RETRY
     *
     *     ... retry
     *
     *      Internal upstream subscribe debug
     *      Internal upstream debug response: $it
     *      Internal upstream debug mapping 0
     *
     * Downstream failed all debug retries: $it
     */
    override fun debugHandleErrorMultistream(): Completable =
        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { Timber.i("Multistream subscribe on debug") }
            .doOnSuccess { Timber.v("Upstream debug response: $it") }
            .doOnError { Timber.v("Upstream failed al debug retries: $it") }
            .map { Timber.v("Upstream debug mapping 0"); it }
            .handleError(count = 4)
            .map { Timber.d("Downstream debug mapping 0"); it }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { Timber.v("Internal upstream subscribe debug") }
                    .doOnSuccess { Timber.v("Internal upstream debug response: $it") }
                    .doOnError { Timber.v("Internal upstream failed all retries: $it") }
                    .map { Timber.v("Internal upstream debug mapping 0"); it }
                    .handleError(count = 4)
                    .map { Timber.d("Internal downstream debug mapping 0"); it }
            }
            .map { Timber.d("Downstream debug mapping 1"); it }
            .doOnSuccess { Timber.d("Downstream debug response: $it") }
            .doOnError { Timber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()

    /**
     * Upstream subscribe on debug
     * Upstream debug response: $it
     * Upstream debug mapping 0
     * ERROR -> RETRY
     *
     * ... retry
     *
     * Upstream subscribe on debug
     * Upstream debug response: $it
     * Upstream debug mapping 0
     * ERROR -> RETRY
     *
     * Downstream failed all debug retries: $it
     */
    override fun debugHandleErrorUpstream(): Completable =
        Single.just(BaseResponse(errorCode = "UpstreamDebugError", errorMessage = "Upstream Debug error"))
            .doOnSubscribe { Timber.i("Upstream subscribe on debug") }
            .doOnSuccess { Timber.v("Upstream debug response: $it") }
            .doOnError { Timber.v("Upstream failed all debug retries: $it") }
            .map { Timber.v("Upstream debug mapping 0"); it }
            .handleError(count = 4)
            .map { Timber.d("Downstream debug mapping 0"); it }
            .doOnSuccess { Timber.d("Downstream debug response: $it") }
            .doOnError { Timber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()

    /**
     * Stream subscribe on debug
     * Upstream debug response: $it
     *     Internal upstream subscribe debug
     *     Internal upstream debug response: $it
     *     Internal upstream debug mapping 0
     *     ERROR -> RETRY
     *
     * ... retry
     *
     *     Internal upstream subscribe debug
     *     Internal upstream debug response: $it
     *     Internal upstream debug mapping 0
     *     ERROR -> RETRY
     *
     * Downstream failed all debug retries: $it
     */
    override fun debugHandleErrorStream(): Completable =
        Single.just(BaseResponse() /** successful response */)
            .doOnSubscribe { Timber.i("Stream subscribe on debug") }
            .doOnSuccess { Timber.v("Upstream debug response: $it") }
            .doOnError { Timber.v("Upstream failed al debug retries: $it") }
            .flatMap {
                Single.just(BaseResponse(errorCode = "DownstreamDebugError", errorMessage = "Downstream Debug error"))
                    .doOnSubscribe { Timber.v("Internal upstream subscribe debug") }
                    .doOnSuccess { Timber.v("Internal upstream debug response: $it") }
                    .doOnError { Timber.v("Internal upstream failed all retries: $it") }
                    .map { Timber.v("Internal upstream debug mapping 0"); it }
                    .handleError(count = 4)
                    .map { Timber.d("Internal downstream debug mapping 0"); it }
            }
            .map { Timber.d("Downstream debug mapping 0"); it }
            .doOnSuccess { Timber.d("Downstream debug response: $it") }
            .doOnError { Timber.d("Downstream failed all debug retries: $it") }
            .ignoreElement()

    // ------------------------------------------
    private var manualPage: Int = 0
    private var manualRetryAttempt: Int = 0

    private fun getAndIncrementManualRetryAttempt(): Int = manualRetryAttempt++

    override fun debugGetNewFaces(page: Int): Single<Feed> =
        Single.just(getFeed(page))
            .doOnSubscribe { Timber.v("Debug Feed: page: $page") }
            .filterAlreadySeenProfilesFeed()
            .filterBlockedProfilesFeed()
            .cacheNewFacesAsAlreadySeen()

    override fun debugGetNewFacesFail(): Single<Feed> = Single.error(SimulatedException())

    override fun debugGetNewFacesFailAndRecoverAfterNTimes(count: Int): Single<Feed> =
        Single.just(0L)
            .flatMap {
                val i = getAndIncrementManualRetryAttempt()
                if (i < count) Single.error(SimulatedException())
                else Single.just(getFeed(manualPage++))  // return some page
            }

    override fun dropFlags(): Completable =
        Single.just(0L)
            .doOnSubscribe {
                manualPage = 0
                manualRetryAttempt = 0
            }
            .ignoreElement()  // convert to Completable

    // ------------------------------------------
    private fun Single<Feed>.cacheNewFacesAsAlreadySeen(): Single<Feed> =
        doOnSuccess { alreadySeenProfilesCache.addProfileModelIds(it.profiles) }

    private fun getAlreadySeenProfileIds(): Single<List<String>> =
        alreadySeenProfilesCache.profileIds()

    private fun getBlockedProfileIds(): Single<List<String>> = blockedProfilesCache.profileIds()

    private fun Single<Feed>.filterAlreadySeenProfilesFeed(): Single<Feed> =
        filterProfilesFeed(idsSource = getAlreadySeenProfileIds())

    private fun Single<Feed>.filterBlockedProfilesFeed(): Single<Feed> =
        filterProfilesFeed(idsSource = getBlockedProfileIds())

    private fun Single<Feed>.filterProfilesFeed(idsSource: Single<List<String>>): Single<Feed> =
        zipWith(idsSource,
            BiFunction { feed: Feed, blockedIds: List<String> ->
                blockedIds
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val l = feed.profiles.toMutableList().apply { removeAll { it.id in blockedIds } }
                        feed.copyWith(profiles = l)
                    } ?: feed
            })
}
