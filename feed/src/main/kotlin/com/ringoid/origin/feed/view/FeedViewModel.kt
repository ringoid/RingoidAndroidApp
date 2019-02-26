package com.ringoid.origin.feed.view

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.SentryUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.actions.*
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.collection.EqualRange
import com.uber.autodispose.lifecycle.autoDisposable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

abstract class FeedViewModel(
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    private val countUserImagesUseCase: CountUserImagesUseCase,
    private val dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase, app: Application)
    : BaseViewModel(app) {

    private var prevRange: EqualRange<ProfileImageVO>? = null
    private val viewActionObjectBuffer = mutableMapOf<Pair<String, String>, ViewActionObject>()

    private val openChatTimers = mutableMapOf<Pair<String, String>, Long>()

    abstract fun getFeed()
    abstract fun getFeedName(): String

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCleared() {
        super.onCleared()
        advanceAndPushViewObjects()
        prevRange = null
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventNoImagesOnProfile(event: BusEvent.NoImagesOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // deleting all images on Profile screen leads any Feed screen to purge it's content
        clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)

        dropLmmChangedStatusUseCase.source()  // drop changed status (red dot badges)
            .autoDisposable(this)
            .subscribe({ Timber.d("Badges on Lmm have been dropped because no images in user's profile") }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    open fun clearScreen(mode: Int) {
        viewState.value = ViewState.CLEAR(mode)
    }

    fun onStartRefresh() {
        actionObjectPool.trigger()
    }

    open fun onRefresh() {
        actionObjectPool.trigger()

        clearCachedAlreadySeenProfileIdsUseCase.source()
            .andThen(
                countUserImagesUseCase.source()
                    .map { it > 0 }  // user has images in profile
                    .doOnSuccess {
                        if (it) {
                            clearScreen(mode = ViewState.CLEAR.MODE_DEFAULT)
                            getFeed()
                        } else {
                            viewState.value = ViewState.DONE(NO_IMAGES_IN_PROFILE)
                        }
                    })
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    fun onLike(profileId: String, imageId: String, isLiked: Boolean) {
        advanceAndPushViewObject(imageId to profileId, recreate = true)
        val aobj = if (isLiked) LikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
                   else UnlikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        actionObjectPool.put(aobj)
    }

    fun onChatOpen(profileId: String, imageId: String) {
        openChatTimers[profileId to imageId] = System.currentTimeMillis()  // record open chat time
    }

    fun onChatClose(profileId: String, imageId: String) {
        val chatTime = openChatTimers
            .takeIf { it.containsKey(profileId to imageId) }
            ?.let { it[profileId to imageId] }
            ?.let { System.currentTimeMillis() - it }
            ?: 0L  // weird, chat was closed but it's open timestamp hadn't been recorded
        advanceAndPushViewObject(imageId to profileId)
        OpenChatActionObject(timeInMillis = chatTime, sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }
    }

    fun onBlock(profileId: String, imageId: String, sourceFeed: String = getFeedName()) {
        onReport(profileId = profileId, imageId = imageId, reasonNumber = 0, sourceFeed = sourceFeed)
    }

    fun onReport(profileId: String, imageId: String, reasonNumber: Int, sourceFeed: String = getFeedName()) {
        advanceAndPushViewObject(imageId to profileId)
        BlockActionObject(numberOfBlockReason = reasonNumber,
            sourceFeed = sourceFeed, targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }

        // remove profile from feed, filter it from backend responses in future
        cacheBlockedProfileIdUseCase.source(params = Params().put("profileId", profileId))
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                ChatInMemoryCache.dropPositionForProfile(profileId = profileId)
                viewState.value = ViewState.DONE(BLOCK_PROFILE(profileId = profileId))
            }, Timber::e)
    }

    fun onView(items: EqualRange<ProfileImageVO>) {
        Timber.v("Incoming visible items: ${items.payloadToString()}")
        prevRange?.delta(items)
            ?.takeIf { !it.isRangeEmpty() }
            ?.let {
                Timber.v("Excluded items in range ${it.range()}, consume VIEW action objects")
                advanceAndPushViewObjects(keys = it.map { it.image.id to it.profileId })
            }

        items.forEach {
            addViewObjectToBuffer(ViewActionObject(
                timeInMillis = 0L, sourceFeed = getFeedName(),
                targetImageId = it.image.id, targetUserId = it.profileId))
        }
        prevRange = items
    }

    // --------------------------------------------------------------------------------------------
    private fun advanceAndPushViewObject(key: Pair<String, String>): ViewActionObject? =
        viewActionObjectBuffer.let {
            val aobj = it[key]?.advance()
            it.remove(key)
            aobj
        }?.also { actionObjectPool.put(it) }

    private fun advanceAndPushViewObject(key: Pair<String, String>, recreate: Boolean) {
        advanceAndPushViewObject(key)
            ?.takeIf { recreate }
            ?.let { addViewObjectToBuffer(it.recreated()) }
    }

    private fun advanceAndPushViewObjects() {
        viewActionObjectBuffer.apply {
            values.forEach { it.advance() ; actionObjectPool.put(it) }
            clear()
        }
    }

    private fun advanceAndPushViewObjects(keys: Collection<Pair<String, String>>) {
        keys.forEach { advanceAndPushViewObject(it) }
    }

    private fun addViewObjectToBuffer(aobj: ViewActionObject) {
        val key = aobj.targetImageId to aobj.targetUserId
        if (viewActionObjectBuffer.containsKey(key)) {
            return  // don't replace already added aobj, because it's creation time is tracking now
        }

        viewActionObjectBuffer[key] = aobj
    }
}
