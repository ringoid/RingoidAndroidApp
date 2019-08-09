package com.ringoid.origin.feed.view

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.actions.BlockActionObject
import com.ringoid.domain.model.actions.LikeActionObject
import com.ringoid.domain.model.actions.ViewActionObject
import com.ringoid.domain.model.actions.ViewChatActionObject
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import com.ringoid.utility.collection.EqualRange
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber

abstract class FeedViewModel(
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    private val cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    private val countUserImagesUseCase: CountUserImagesUseCase,
    protected val filtersSource: IFiltersSource,
    private val userInMemoryCache: IUserInMemoryCache, app: Application)
    : BasePermissionViewModel(app) {

    val refreshOnPush by lazy { MutableLiveData<Boolean>() }

    private var verticalPrevRange: EqualRange<ProfileImageVO>? = null
    private val horizontalPrevRanges = mutableMapOf<String, EqualRange<ProfileImageVO>>()  // profileId : range
    private val viewActionObjectBuffer = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW
    private val viewActionObjectBackup = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW

    private val openChatTimers = mutableMapOf<Pair<String, String>, Long>()
    private var willRestart: Boolean = false

    abstract fun getFeed()
    abstract fun getFeedName(): String

    // --------------------------------------------------------------------------------------------
    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        viewState.value
            .takeIf { it is ViewState.CLEAR }
            ?.let { it as ViewState.CLEAR }
            ?.takeIf { it.mode == ViewState.CLEAR.MODE_EMPTY_DATA }
            ?.let { viewState.value = ViewState.CLEAR(ViewState.CLEAR.MODE_NEED_REFRESH) }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean): Boolean {
        val changed = super.setUserVisibleHint(isVisibleToUser)
        if (changed) {
            handleUserVisibleHint(isVisibleToUser)
        }
        return changed
    }

    protected open fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            DebugLogUtil.v("Show feed '${getFeedName()}'... restore [${viewActionObjectBackup.size}] active VIEWs: ${viewActionObjectBackup.values.joinToString("\n\t\t", "\n\t\t", transform = { it.toActionString() })}")
            viewActionObjectBackup.values.forEach {
                val aobj = ViewActionObject(timeInMillis = 0L, sourceFeed = getFeedName(),
                    targetImageId = it.targetImageId, targetUserId = it.targetUserId)
                addViewObjectToBuffer(aobj)
            }
            viewActionObjectBackup.clear()  // all backup-ed aobjs have been consumed
            // reset time of creation ('actionTime') for hidden action objects as they've became visible now
            viewActionObjectBuffer.values
                .filter { it.sourceFeed == getFeedName() }
                .filter { it.isHidden }
                .map { it.recreated().apply { it.isHidden = false } }
                .forEach { viewActionObjectBuffer[it.key()] = it }
        } else {
            DebugLogUtil.v("Hide feed '${getFeedName()}'... push [${viewActionObjectBuffer.size}] active VIEWs: ${viewActionObjectBuffer.values.joinToString("\n\t\t", "\n\t\t", transform = { it.toActionString() })}")
            advanceAndPushViewObjects(backupPool = viewActionObjectBackup)
            actionObjectPool.trigger()
        }
    }

    // ------------------------------------------
    override fun onStart() {
        super.onStart()
        if (willRestart) {
            willRestart = false
            DebugLogUtil.v("Restarting feed '${getFeedName()}'...")
            setUserVisibleHint(isVisibleToUser = true)
        }
    }

    override fun onStop() {
        super.onStop()
        willRestart = getUserVisibleHint()
        if (willRestart) {
            DebugLogUtil.v("Stopping feed '${getFeedName()}'...")
            setUserVisibleHint(isVisibleToUser = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        advanceAndPushViewObjects()
        actionObjectPool.trigger()
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    internal fun clearScreen(mode: Int) {
        viewActionObjectBackup.clear()
        viewState.value = ViewState.CLEAR(mode)
    }

    internal fun onClearScreen() {
        DebugLogUtil.v("On clear ${getFeedName()} feed")
        horizontalPrevRanges.clear()
        verticalPrevRange = null
    }

    override fun onLocationReceived(handleCode: Int) {
        super.onLocationReceived(handleCode)
        onRefresh()  // request for feed data with potentially updated location data
    }

    internal fun onStartRefresh() {
        analyticsManager.fire(Analytics.PULL_TO_REFRESH, "sourceFeed" to getFeedName())
    }

    protected open fun onRefresh() {
        advanceAndPushViewObjects()

        clearCachedAlreadySeenProfileIdsUseCase.source()
            .andThen(
                countUserImagesUseCase.source()
                    .doOnSuccess {
                        if (checkImagesCount(it)) {
                            clearScreen(mode = ViewState.CLEAR.MODE_DEFAULT)  // purge feed on refresh, before fetching a new one
                            getFeed()
                        } else {
                            viewState.value = ViewState.DONE(NO_IMAGES_IN_PROFILE)
                        }
                    })
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    internal open fun onSettingsClick(profileId: String) {
        // override in subclasses
    }

    protected open fun checkImagesCount(count: Int): Boolean = count > 0

    protected fun refresh() {
        viewState.value = ViewState.DONE(REFRESH)
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    internal fun onBeforeLike(): Boolean =
        if (userInMemoryCache.userImagesCount() > 0) true
        else {
            viewState.value = ViewState.DONE(NO_IMAGES_IN_PROFILE)
            false
        }

    internal open fun onImageTouch(x: Float, y: Float) {
        Timber.v("On touch feed item at ($x, $y)")
        // override in subclasses
    }

    internal open fun onLike(profileId: String, imageId: String) {
        advanceAndPushViewObject(imageId to profileId)
        val aobj = LikeActionObject(sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
        actionObjectPool.put(aobj)

        // analytics
        with (analyticsManager) {
            val sourceFeed = getFeedName()
            fire(Analytics.ACTION_USER_LIKE_PHOTO, "sourceFeed" to sourceFeed)
            when (sourceFeed) {
                DomainUtil.SOURCE_FEED_LIKES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_LIKES)
                DomainUtil.SOURCE_FEED_MATCHES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_MATCHES)
                DomainUtil.SOURCE_FEED_MESSAGES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_MESSAGES)
            }
        }
    }

    internal open fun onChatOpen(profileId: String, imageId: String) {
        /**
         * Need to mark profile as seen before opening chat, because it then malfunctions when
         * feed screen goes background while chat is opening: basically transition to [ViewState.DONE]
         * does not work during background. So, as a workaround, such transition should be done here
         * while feed screen is on foreground and chat not yet opened.
         */
        onViewFeedItem(feedItemId = profileId)
        openChatTimers[profileId to imageId] = System.currentTimeMillis()  // record open chat time
    }

    internal open fun onChatClose(profileId: String, imageId: String) {
        val chatTime = openChatTimers
            .takeIf { it.containsKey(profileId to imageId) }
            ?.let { it[profileId to imageId] }
            ?.let { System.currentTimeMillis() - it }
            ?: 0L  // weird, chat was closed but it's open timestamp hadn't been recorded
        advanceAndPushViewObject(imageId to profileId)
        ViewChatActionObject(timeInMillis = chatTime, sourceFeed = getFeedName(), targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }
    }

    internal open fun onBlock(profileId: String, imageId: String, sourceFeed: String = getFeedName(), fromChat: Boolean = false) {
        onReport(profileId = profileId, imageId = imageId, reasonNumber = 0, sourceFeed = sourceFeed, fromChat = fromChat)
    }

    internal open fun onReport(profileId: String, imageId: String, reasonNumber: Int, sourceFeed: String = getFeedName(), fromChat: Boolean = false) {
        advanceAndPushViewObject(imageId to profileId)
        BlockActionObject(numberOfBlockReason = reasonNumber,
            sourceFeed = sourceFeed, targetImageId = imageId, targetUserId = profileId)
            .also { actionObjectPool.put(it) }

        analyticsManager.fire(Analytics.ACTION_USER_BLOCK_OTHER, "reason" to "$reasonNumber", "sourceFeed" to sourceFeed, "fromChat" to "$fromChat")

        // remove profile from feed, filter it from backend responses in future
        cacheBlockedProfileIdUseCase.source(params = Params().put("profileId", profileId))
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                ChatInMemoryCache.dropPositionForProfile(profileId = profileId)
                viewState.value = ViewState.DONE(DISCARD_PROFILE(profileId = profileId))
            }, Timber::e)

        // remove all messages for blocked profile, to exclude them from messages counting
        clearMessagesForChatUseCase.source(params = Params().put("chatId", profileId))
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ ChatInMemoryCache.deleteProfile(profileId) }, Timber::e)

        // remove VIEW aobj associated with blocked profile from backup to prevent it from restoring
        viewActionObjectBackup.remove(imageId to profileId)
    }

    internal fun onViewHorizontal(items: EqualRange<ProfileImageVO>) {
        Timber.v("Incoming visible items [horizontal, ${getFeedName()}]: ${items.payloadToString()}")
        items.pickOne()?.let {
            horizontalPrevRanges[it.profileId]
                ?.delta(items)
                ?.takeIf { !it.isRangeEmpty() }
                ?.let { range ->
                    Timber.v("Excluded items in [horizontal] range ${range.range()}, consume VIEW action objects")
                    logViewObjectsBufferState(tag = "before [horiz]")  // show view aobjs buffer contents in debug logs
                    advanceAndPushViewObjects(keys = range.map { it.image.id to it.profileId })
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_SWIPE, "sourceFeed" to getFeedName())
                    logViewObjectsBufferState(tag = "after [horiz]")
                }
        }

        addViewObjectsToBuffer(items, tag = "[horiz]")
        items.pickOne()?.let { horizontalPrevRanges[it.profileId] = items }
    }

    internal fun onViewVertical(items: EqualRange<ProfileImageVO>) {
        Timber.v("Incoming visible items [vertical, ${getFeedName()}]: ${items.payloadToString()}")
        verticalPrevRange
            ?.delta(items)
            ?.takeIf { !it.isRangeEmpty() }
            ?.let { range ->
                Timber.v("Excluded items in [vertical] range ${range.range()}, consume VIEW action objects")
                logViewObjectsBufferState("before [vert]")  // show view aobjs buffer contents in debug logs
                range.pickOne()
                    ?.let { horizontalPrevRanges[it.profileId] }
                    ?.also { advanceAndPushViewObjects(keys = it.map { it.image.id to it.profileId }) }
                    ?.also { logViewObjectsBufferState(tag = "after [vert]") }
            }

        /**
         * As imageId is not correctly supplied to items in incoming [items] range, because it's not
         * feasible to access current image on current feed item in scroll callback (viewHolder is
         * not accessible from outside), here we need to fixup those imageIds for each item in [items].
         * It's possible because we keep track on what images we are looking at while scrolling
         * horizontally.
         */
        val fixItems = items.map { ProfileImageVO(it.profileId, image = horizontalPrevRanges[it.profileId]?.pickOne()?.image ?: it.image) }
        val fixRange = EqualRange(from = items.from, to = items.to, items = fixItems)
        addViewObjectsToBuffer(fixRange, tag = "[vert]")
        items.filter { !horizontalPrevRanges.containsKey(it.profileId) }
             .forEach { horizontalPrevRanges[it.profileId] = EqualRange(from = 0, to = 0, items = listOf(it)) }
        verticalPrevRange = fixRange
    }

    /**
     * Profile represented as [FeedItem] instance has been seen by user.
     */
    protected open fun onViewFeedItem(feedItemId: String) {
        DebugLogUtil.v("On View profile [${getFeedName()}]: ${feedItemId.substring(0..3)}")
        // do something when feed item with id specified has been viewed
    }

    internal fun onItemBecomeVisible(profile: FeedItemVO, image: IImage) {
        DebugLogUtil.v("Item become visible [${getFeedName()}]: p=${profile.id.substring(0..3)}")
        if (!horizontalPrevRanges.containsKey(profile.id)) {
            horizontalPrevRanges[profile.id] = EqualRange(0, 0, listOf(ProfileImageVO(profileId = profile.id, image = image)))
        }

        val aobj = ViewActionObject(timeInMillis = 0L, sourceFeed = getFeedName(),
            targetImageId = image.id, targetUserId = profile.id)
        addViewObjectToBuffer(aobj)
    }

    open fun onDiscardProfile(profileId: String) {
        horizontalPrevRanges.remove(profileId)  // feed item has gone

        advanceAndPushViewObject(profileId = profileId)  // push VIEW as profile was discarded

        viewActionObjectBackup.keys
            .find { it.second == profileId }
            ?.let { viewActionObjectBackup.remove(it) }
    }

    /**
     * After discard feed item, feed collapses vertically so there could be a new feed item coming
     * into viewport. [verticalPrevRange] should be updated with that item and discarded item should
     * be gone, so that new [onViewVertical] during scroll will operate with correct feed items.
     */
    internal fun onSettleVisibleItemsAfterDiscard(items: List<FeedItemVO>) {
        val fixItems = items.map { ProfileImageVO(it.id, image = horizontalPrevRanges[it.id]?.pickOne()?.image ?: it.images[it.positionOfImage]) }
        DebugLogUtil.v("Settle on discard [vert], before: $verticalPrevRange ${verticalPrevRange?.joinToString { it.profileId.substring(0..3) }}")
        verticalPrevRange = verticalPrevRange?.copyWith(fixItems)
        DebugLogUtil.v("Settle on discard [vert], after: $verticalPrevRange ${verticalPrevRange?.joinToString { it.profileId.substring(0..3) }}")
    }

    // --------------------------------------------------------------------------------------------
    /**
     * Advance and push whatever VIEW object corresponds to [FeedItem] with [FeedItem.id] == [profileId], if any.
     */
    private fun advanceAndPushViewObject(profileId: String) {
        viewActionObjectBuffer.keys
            .find { it.second == profileId }
            ?.let { advanceAndPushViewObject(it) }
    }

    private fun advanceAndPushViewObject(key: Pair<String, String>): ViewActionObject? =
        advanceViewObject(key)?.also { actionObjectPool.put(it) }

    private fun advanceAndPushViewObject(key: Pair<String, String>, recreate: Boolean) {
        advanceAndPushViewObject(key)
            ?.takeIf { recreate }
            ?.let { addViewObjectToBuffer(it.recreated()) }
    }

    private fun advanceAndPushViewObjects(backupPool: MutableMap<Pair<String, String>, ViewActionObject>? = null) {
        viewActionObjectBuffer.apply {
            mutableSetOf<String>()
                .apply { keys.forEach { add(it.second) } }
                .forEach { onViewFeedItem(feedItemId = it) }

            values.forEach { it.advance() }
            actionObjectPool.put(values)  // add all aobjs at once
            backupPool?.putAll(this)
            clear()
        }
    }

    private fun advanceAndPushViewObjects(keys: Collection<Pair<String, String>>) {
        val aobjs = mutableListOf<ViewActionObject>()
        keys.forEach { key -> advanceViewObject(key)?.also { aobjs.add(it) } }
        actionObjectPool.put(aobjs)  // add all aobjs at once
    }

    // ------------------------------------------
    private fun advanceViewObject(key: Pair<String, String>): ViewActionObject? =
        viewActionObjectBuffer.let {
            val aobj = it[key]?.advance()
            it.remove(key)
            aobj as? ViewActionObject
        }
        ?.also { onViewFeedItem(it.targetUserId) }

    // ------------------------------------------
    private fun logViewObjectsBufferState(tag: String) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DebugLogUtil.d("View buffer [$tag]:\n${viewActionObjectBuffer.values.joinToString("\n\t\t","\t\t", transform = { it.toActionString() })}")
        }
    }

    // ------------------------------------------
    private fun addViewObjectsToBuffer(items: EqualRange<ProfileImageVO>, tag: String = "") {
        items.forEach {
            if (it.image.isRealModel) {
                val aobj = ViewActionObject(timeInMillis = 0L, sourceFeed = getFeedName(),
                    targetImageId = it.image.id, targetUserId = it.profileId)
                aobj.isHidden = !getUserVisibleHint()
                addViewObjectToBuffer(aobj, tag = tag)
            }
        }
    }

    private fun addViewObjectToBuffer(aobj: ViewActionObject, tag: String = "") {
        val key = aobj.key()
        if (viewActionObjectBuffer.containsKey(key)) {
            return  // don't replace already added aobj, because it's creation time is tracking now
        }

        DebugLogUtil.v("Create${" $tag".trim()}: ${aobj.toActionString()}")
        viewActionObjectBuffer[key] = aobj
    }
}
