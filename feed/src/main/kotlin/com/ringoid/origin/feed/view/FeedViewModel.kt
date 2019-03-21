package com.ringoid.origin.feed.view

import android.app.Application
import android.os.Build
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.log.SentryUtil
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

    private var verticalPrevRange: EqualRange<ProfileImageVO>? = null
    private val horizontalPrevRanges = mutableMapOf<String, EqualRange<ProfileImageVO>>()  // profileId : range
    private val viewActionObjectBuffer = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW
    private val viewActionObjectBackup = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW

    private val openChatTimers = mutableMapOf<Pair<String, String>, Long>()

    abstract fun getFeed()
    abstract fun getFeedName(): String

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            DebugLogUtil.v("Show feed '${getFeedName()}'... restore [${viewActionObjectBackup.size}] active VIEWs: ${viewActionObjectBackup.values.joinToString("\n\t\t", "\n\t\t", transform = { it.toActionString() })}")
            viewActionObjectBackup.values.forEach {
                val aobj = ViewActionObject(timeInMillis = 0L, sourceFeed = getFeedName(),
                    targetImageId = it.targetImageId, targetUserId = it.targetUserId)
                addViewObjectToBuffer(aobj)
            }
            viewActionObjectBackup.clear()  // all backup-ed aobjs have been consumed
        } else {
            DebugLogUtil.v("Hide feed '${getFeedName()}'... push [${viewActionObjectBuffer.size}] active VIEWs: ${viewActionObjectBuffer.values.joinToString("\n\t\t", "\n\t\t", transform = { it.toActionString() })}")
            advanceAndPushViewObjects(backupPool = viewActionObjectBackup)
            actionObjectPool.trigger()
        }
    }

    override fun onCleared() {
        super.onCleared()
        advanceAndPushViewObjects()
        actionObjectPool.trigger()
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
        advanceAndPushViewObjects()
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
        ViewChatActionObject(timeInMillis = chatTime, sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
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

    fun onViewHorizontal(items: EqualRange<ProfileImageVO>) {
        if (!getUserVisibleHint()) {
            Timber.v("Ignore incoming visible items [horizontal]: ${getFeedName()} is not visible")
            return
        }

        Timber.v("Incoming visible items [horizontal, ${getFeedName()}]: ${items.payloadToString()}")
        items.pickOne()?.let {
            horizontalPrevRanges[it.profileId]
                ?.delta(items)
                ?.takeIf { !it.isRangeEmpty() }
                ?.let {
                    Timber.v("Excluded items in [horizontal] range ${it.range()}, consume VIEW action objects")
                    logViewObjectsBufferState()  // show view aobjs buffer contents in debug logs
                    advanceAndPushViewObjects(keys = it.map { it.image.id to it.profileId })
                }
        }

        addViewObjectsToBuffer(items)
        items.pickOne()?.let { horizontalPrevRanges[it.profileId] = items }
    }

    fun onViewVertical(items: EqualRange<ProfileImageVO>) {
        if (!getUserVisibleHint()) {
            Timber.v("Ignore incoming visible items [vertical]: ${getFeedName()} is not visible")
            return
        }

        Timber.v("Incoming visible items [vertical, ${getFeedName()}]: ${items.payloadToString()}")
        verticalPrevRange
            ?.delta(items)
            ?.takeIf { !it.isRangeEmpty() }
            ?.let {
                Timber.v("Excluded items in [vertical] range ${it.range()}, consume VIEW action objects")
                logViewObjectsBufferState()  // show view aobjs buffer contents in debug logs
                it.pickOne()
                    ?.let { horizontalPrevRanges[it.profileId] }
                    ?.also { advanceAndPushViewObjects(keys = it.map { it.image.id to it.profileId }) }
            }

        /**
         * As imageId is not correctly supplied to items in incoming [items] range, because it's not
         * feasible to access current image on current feed item in scroll callback (viewHolder is
         * not accessible from outside), here we need to fixup those imageIds for each item in [items].
         * It's possible because we keep track on what images we are looking at while scrolling
         * horizontally.
         */
        val fixItems = items.map { ProfileImageVO(it.profileId, image = horizontalPrevRanges[it.profileId]?.pickOne()?.image ?: it.image, isLiked = it.isLiked) }
        val fixRange = EqualRange(from = items.from, to = items.to, items = fixItems)
        addViewObjectsToBuffer(fixRange)
        verticalPrevRange = fixRange
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

    private fun advanceAndPushViewObjects(backupPool: MutableMap<Pair<String, String>, ViewActionObject>? = null) {
        viewActionObjectBuffer.apply {
            values.forEach { it.advance() ; actionObjectPool.put(it) }
            backupPool?.putAll(this)
            clear()
        }
        horizontalPrevRanges.clear()
        verticalPrevRange = null
    }

    private fun advanceAndPushViewObjects(keys: Collection<Pair<String, String>>) {
        keys.forEach { advanceAndPushViewObject(it) }
    }

    // ------------------------------------------
    private fun logViewObjectsBufferState() {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DebugLogUtil.d("View buffer:\n${viewActionObjectBuffer.values.joinToString("\n\t\t","\t\t", transform = { it.toActionString() })}")
        }
    }

    // ------------------------------------------
    private fun addViewObjectsToBuffer(items: EqualRange<ProfileImageVO>) {
        items.forEach {
            if (it.image.isRealModel) {
                val aobj = ViewActionObject(timeInMillis = 0L, sourceFeed = getFeedName(),
                    targetImageId = it.image.id, targetUserId = it.profileId)
                addViewObjectToBuffer(aobj)
            }
        }
    }

    private fun addViewObjectToBuffer(aobj: ViewActionObject) {
        val key = aobj.targetImageId to aobj.targetUserId
        if (viewActionObjectBuffer.containsKey(key)) {
            return  // don't replace already added aobj, because it's creation time is tracking now
        }

        DebugLogUtil.v("Create: ${aobj.toActionString()}")
        viewActionObjectBuffer[key] = aobj
    }
}
