package com.ringoid.origin.feed.view

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
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
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import com.ringoid.utility.cloneAsList
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.targetVersion
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import timber.log.Timber

abstract class FeedViewModel(
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    private val cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    protected val countUserImagesUseCase: CountUserImagesUseCase,
    protected val filtersSource: IFiltersSource,
    private val userInMemoryCache: IUserInMemoryCache, app: Application)
    : BasePermissionViewModel(app) {

    protected val discardProfileOneShot by lazy { MutableLiveData<OneShot<String>>() }
    private val likeProfileOneShot by lazy { MutableLiveData<OneShot<Int>>() }
    protected val needShowFiltersOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val noImagesInUserProfileOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    protected val notifyOnFeedLoadFinishOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val refreshOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val refreshOnLocationPermissionOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    protected val refreshOnPush by lazy { MutableLiveData<Boolean>() }
    internal fun discardProfileOneShot(): LiveData<OneShot<String>> = discardProfileOneShot
    internal fun likeProfileOneShot(): LiveData<OneShot<Int>> = likeProfileOneShot
    internal fun needShowFiltersOneShot(): LiveData<OneShot<Boolean>> = needShowFiltersOneShot
    internal fun noImagesInUserProfileOneShot(): LiveData<OneShot<Boolean>> = noImagesInUserProfileOneShot
    internal fun notifyOnFeedLoadFinishOneShot(): LiveData<OneShot<Boolean>> = notifyOnFeedLoadFinishOneShot
    internal fun refreshOneShot(): LiveData<OneShot<Boolean>> = refreshOneShot
    internal fun refreshOnLocationPermissionOneShot(): LiveData<OneShot<Boolean>> = refreshOnLocationPermissionOneShot
    internal fun refreshOnPush(): LiveData<Boolean> = refreshOnPush
    internal fun isRefreshOnPush(): Boolean = refreshOnPush.value == true

    private var verticalPrevRange: EqualRange<ProfileImageVO>? = null
    private val horizontalPrevRanges = mutableMapOf<String, EqualRange<ProfileImageVO>>()  // profileId : range
    private val viewActionObjectBuffer = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW
    private val viewActionObjectBackup = mutableMapOf<Pair<String, String>, ViewActionObject>()  // imageId, profileId : VIEW

    private val openChatTimers = mutableMapOf<Pair<String, String>, Long>()
    private var willRestart: Boolean = false

    abstract fun getFeed(): Completable
    abstract fun getFeedName(): String

    // --------------------------------------------------------------------------------------------
    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        viewState.value.takeIf { it is ViewState.CLEAR }?.let { it as ViewState.CLEAR }
            ?.takeIf { it.mode == ViewState.CLEAR.MODE_EMPTY_DATA }  // before tab select there was no data
            ?.let { viewState.value = ViewState.CLEAR(ViewState.CLEAR.MODE_NEED_REFRESH) }  // before tab select change clear mode
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleVisibleHintChange(isVisibleToUser: Boolean) {
        super.handleVisibleHintChange(isVisibleToUser)
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
        }
    }

    // ------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalNavigator.RC_SETTINGS_LOCATION -> refresh()  // refresh on change location settings
        }
    }

    // ------------------------------------------
    override fun onStart() {
        super.onStart()
        if (willRestart) {
            willRestart = false
            DebugLogUtil.v("Restarting feed '${getFeedName()}'...")
        }
    }

    override fun onStop() {
        super.onStop()
        willRestart = getUserVisibleHint()
        if (willRestart) {
            DebugLogUtil.v("Stopping feed '${getFeedName()}'...")
        }
    }

    override fun onCleared() {
        super.onCleared()
        advanceAndPushViewObjects { actionObjectPool.trigger() }  // trigger before destroy Feed screen
    }

    /* Feed */
    // --------------------------------------------------------------------------------------------
    internal fun onClearScreen() {
        DebugLogUtil.v("On clear ${getFeedName()} feed")
        horizontalPrevRanges.clear()
        verticalPrevRange = null
        viewActionObjectBackup.clear()
    }

    override fun onLocationPermissionDeniedAction(handleCode: Int) {
        super.onLocationPermissionDeniedAction(handleCode)
        viewState.value = ViewState.CLEAR(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // location permission denied
    }

    override fun onLocationReceived(handleCode: Int) {
        super.onLocationReceived(handleCode)
        refreshOnLocationPermissionOneShot.value = OneShot(true)
    }

    internal fun onStartRefresh() {
        analyticsManager.fire(Analytics.PULL_TO_REFRESH, "sourceFeed" to getFeedName())
    }

    internal open fun onRefresh() {
        advanceAndPushViewObjects()

        clearCachedAlreadySeenProfileIdsUseCase.source()
            .andThen(countUserImagesUseCase.source())
            .flatMapCompletable { count ->
                if (checkImagesCount(count)) {
                    viewActionObjectBackup.clear()
                    getFeed()
                } else {
                    noImagesInUserProfileOneShot.value = OneShot(false)
                    Completable.complete()
                }
            }
            .autoDisposable(this)
            .subscribe({}, DebugLogUtil::e)
    }

    internal open fun onSettingsClick(profileId: String) {
        // override in subclasses
    }

    protected open fun checkImagesCount(count: Int): Boolean = count > 0

    internal fun refresh() {
        DebugLogUtil.d("Refreshing Feed [${getFeedName()}]")
        refreshOneShot.value = OneShot(true)
    }

    /* Action Objects */
    // --------------------------------------------------------------------------------------------
    private fun hasUserImages(): Boolean = userInMemoryCache.userImagesCount() > 0

    // ------------------------------------------
    private var feedItemToLikePosition: Int = DomainUtil.BAD_POSITION

    internal fun onBeforeLike(position: Int): Boolean =
        if (hasUserImages()) {
            true
        } else {
            feedItemToLikePosition = position
            noImagesInUserProfileOneShot.value = OneShot(true)
            false
        }

    internal fun onCancelNoImagesInUserProfileDialog() {
        dropFeedItemToLikePosition()
    }

    internal fun doPendingLikeInAny() {
        if (feedItemToLikePosition == DomainUtil.BAD_POSITION) {
            return  // no pending like to perform
        }

        if (!hasUserImages()) {
            /**
             * User has intended to like someone's profile but was interrupted by asking to add image
             * on Profile. If user still has no images in her profile and has just navigated back on
             * this Feed screen - forget that intention.
             */
            dropFeedItemToLikePosition()
            return  // do pending like only if user has some images in profile
        }
        if (feedItemToLikePosition != DomainUtil.BAD_POSITION) {
            likeProfileOneShot.value = OneShot(feedItemToLikePosition)
            dropFeedItemToLikePosition()
        }
    }

    private fun dropFeedItemToLikePosition() {
        feedItemToLikePosition = DomainUtil.BAD_POSITION
    }

    // ------------------------------------------
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
                DomainUtil.SOURCE_SCREEN_FEED_LIKES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_LIKES)
                DomainUtil.SOURCE_SCREEN_FEED_MATCHES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_MATCHES)
                DomainUtil.SOURCE_SCREEN_FEED_MESSAGES -> fire(Analytics.ACTION_USER_LIKE_PHOTO_FROM_MESSAGES)
            }
        }
    }

    internal open fun onChatOpen(profileId: String, imageId: String) {
        /**
         * Need to mark profile as seen before opening chat, because it then malfunctions when
         * feed screen goes background while chat is opening: basically setting [viewState] liveData
         * does not work during background. So, as a workaround, such transition should be done here
         * while feed screen is on foreground and chat not yet opened.
         */
        onSeenFeedItem(feedItemId = profileId)
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
            .doOnError { viewState.value = ViewState.ERROR(it) }  // cache blocked peer id failed
            .autoDisposable(this)
            .subscribe({
                ChatInMemoryCache.dropPositionForProfile(profileId = profileId)
                discardProfileOneShot.value = OneShot(profileId)
            }, DebugLogUtil::e)

        // remove all messages for blocked profile, to exclude them from messages counting
        clearMessagesForChatUseCase.source(params = Params().put("chatId", profileId))
            .doOnError { viewState.value = ViewState.ERROR(it) }  // clear local chat for blocked peer failed
            .autoDisposable(this)
            .subscribe({ ChatInMemoryCache.deleteProfile(profileId) }, DebugLogUtil::e)

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
    protected open fun onSeenFeedItem(feedItemId: String) {
        DebugLogUtil.v("On seen profile [${getFeedName()}]: ${feedItemId.substring(0..3)}")
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

    private fun advanceAndPushViewObjects(
            backupPool: MutableMap<Pair<String, String>, ViewActionObject>? = null,
            onComplete: (() -> Unit)? = null) {
        with (viewActionObjectBuffer) {
            mutableSetOf<String>()
                .apply { keys.forEach { add(it.second) } }
                .forEach { onSeenFeedItem(feedItemId = it) }

            values.forEach { it.advance() }
            /**
             * Since [IActionObjectPool.put] is performed asynchronously, it should be passed in
             * an immutable copy of collection of action objects, and then it could be cleared on
             * the current thread.
             */
            actionObjectPool.put(values.cloneAsList(), onComplete)  // add all aobjs at once
            backupPool?.putAll(this)
            clear()  // <-- this clears [values] on the current thread
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
            it.remove(key)  // remove aobjs from collection, but still return it below
            aobj as? ViewActionObject
        }
        ?.also { onSeenFeedItem(it.targetUserId) }

    // ------------------------------------------
    /**
     * Checks whether [FeedItemVO] given by [profileId] is currently visible on screen,
     * so it's [ViewActionObject] is buffered, but not yet pushed.
     */
    protected fun isFeedItemOnViewport(profileId: String): Boolean =
        viewActionObjectBuffer.keys.find { it.second == profileId } != null

    private fun logViewObjectsBufferState(tag: String) {
        if (BuildConfig.DEBUG && targetVersion(Build.VERSION_CODES.O)) {
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
