package com.ringoid.origin.feed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.touches
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.ViewState
import com.ringoid.base.view.VisibleHint
import com.ringoid.debug.DebugLogUtil
import com.ringoid.debug.timer.TimeKeeper
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.widget.FiltersPopupWidget
import com.ringoid.origin.feed.view.widget.ToolbarWidget
import com.ringoid.origin.model.FeedItemContextMenuPayload
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.base.BaseListFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.IEmptyScreenCallback
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.report.log.Report
import com.ringoid.utility.*
import com.ringoid.utility.collection.EqualRange
import com.ringoid.widget.view._swipes
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

abstract class FeedFragment<VM : FeedViewModel> : BaseListFragment<VM>(), IEmptyScreenCallback {

    protected lateinit var feedAdapter: BaseFeedAdapter
        private set
    private lateinit var feedTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>
    private lateinit var imagesTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    protected var filtersPopupWidget: FiltersPopupWidget? = null
    private var toolbarWidget: ToolbarWidget? = null

    protected val timeKeeper = TimeKeeper()

    // ------------------------------------------
    protected abstract fun contextMenuActions(): String

    override fun getLayoutId(): Int = R.layout.fragment_feed
    override fun getRecyclerView(): RecyclerView = rv_items

    protected abstract fun createFeedAdapter(): BaseFeedAdapter
    protected abstract fun createFiltersFragment(): BaseFiltersFragment<*>
    @StringRes protected abstract fun getAddPhotoDialogDescriptionResId(): Int
    @StringRes protected abstract fun getToolbarTitleResId(): Int

    protected abstract fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input?

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onErrorState() {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // error state on Feed
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> onClearState(mode = newState.mode)
            is ViewState.IDLE -> {
                fl_empty_container?.changeVisibility(isVisible = false)
                showLoading(isVisible = false)
            }
            is ViewState.LOADING -> showLoading(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState) { vm.refresh() /** refresh on connection timeout */ }
        }
    }

    override fun onVisibleHintChange(newHint: VisibleHint) {
        super.onVisibleHintChange(newHint)
        when (newHint) {
            VisibleHint.VISIBLE -> view?.delay { trackVisibilityOnListInsert() }
            else -> { /* no-op */ }
        }
    }

    protected open fun onClearState(mode: Int) {
        fun showEmptyStub(input: EmptyFragment.Companion.Input? = null) {
            fl_empty_container?.let {
                it.changeVisibility(isVisible = true)
                val emptyFragment = EmptyFragment.newInstance(input)
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
        }

        feedAdapter.clear()  // on MODE_DEFAULT - just clear adapter items
        toolbarWidget?.show()  // show toolbar when feed is empty
        clearScrollData()

        getEmptyStateInput(mode)?.let {
            showEmptyStub(input = it)
            showLoading(isVisible = false)
        } ?: run { fl_empty_container?.changeVisibility(isVisible = false) }

        if (isViewModelInitialized) {
            vm.onClearScreen()
        }
    }

    private fun getVisibleItems(excludedId: String? = null): List<FeedItemVO> =
        rv_items.linearLayoutManager()?.let { lm ->
            val from = lm.findFirstVisibleItemPosition()
            val to = lm.findLastVisibleItemPosition()
            if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION) {
                feedAdapter.getModelsInRange(from, to)
                    .apply { excludedId?.let { exId -> removeAll { it.id == exId } } }
                    .toList()
            } else emptyList()
        } ?: emptyList()

    private fun getVisibleItemIds(excludedId: String? = null): List<String> =
        getVisibleItems(excludedId).map { it.id }

    private fun checkForNewlyVisibleItems(prevIds: Collection<String>, newIds: Collection<String>, excludedId: String? = null) {
        newIds.toMutableList()
            .also { list ->  // for all items that are visible after discard - check scroll offsets and apply strategies
                list.forEach { id ->  // this will make labels visible depending on scroll offsets for each item
                    feedAdapter.findPosition { it.id == id }
                               .takeIf { it > DomainUtil.BAD_POSITION }
                               ?.also { Timber.v("Check scroll offset for item $id at position: $it") }
                               ?.let { position -> trackScrollOffsetForPosition(position) }
                }
            }
            .apply { removeAll(prevIds) }  // retain only new items compared to previous ones
            .also { DebugLogUtil.d("Discarded ${excludedId?.substring(0..3)}, became visible[${it.size}]: ${it.joinToString { it.substring(0..3) }}") }
            .takeIf { it.isNotEmpty() }
            ?.forEach { id ->  // retained items are the new ones that come into viewport, i.e. became visible
                feedAdapter.findModelAndPosition { it.id == id }
                    ?.let { (_, model) ->
                        val image = model.images[model.positionOfImage]
                        vm.onItemBecomeVisible(profile = model, image = image)
                    }
            }
    }

    override fun onAskToEnableLocationService() {
        super.onAskToEnableLocationService()
        onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // purge Feed while displaying ask to enable GPS popup
    }

    protected open fun onDiscardAllProfiles() {
        onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)  // discard all profiles in Feed
    }

    /**
     * User discards profile manually (via transition (LIKE) or block (BLOCK / REPORT),
     * so need to handle VIEW aobjs for profiles that comes into viewport after removal
     * animation finishes.
     */
    protected open fun onDiscardProfile(profileId: String): FeedItemVO? =
        feedAdapter.findModelAndPosition { it.id == profileId }
            ?.also { (position, _) ->
                val count = feedAdapter.getModelsCount()
                if (count <= 1) {  // remove last feed item - show empty stub directly
                    onDiscardAllProfiles()
                } else {  // remove not last feed item
                    val prevIds = getVisibleItemIds(profileId)  // record ids of visible items before remove
                    DebugLogUtil.d("Discard item ${profileId.substring(0..3)}, visible BEFORE[${prevIds.size}]: ${prevIds.joinToString { it.substring(0..3) }}")

                    /**
                     * After finishing item remove animation, detect what items come into viewport
                     * and call [FeedViewModel.onItemBecomeVisible] on each of them.
                     */
                    rv_items.itemAnimator
                        .let { it as FeedItemAnimator }
                        .removeAnimationFinishedSource()
                        .delay(100L, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                        .take(1)  // single-shot subscription
                        .doOnSubscribe { localScopeProvider.start() }
                        .doOnDispose { DebugLogUtil.v("Discard item ${profileId.substring(0..3)}: disposed local subscription") }
                        .doFinally {
                            DebugLogUtil.v("Discard item ${profileId.substring(0..3)} has completed")
                            localScopeProvider.stop()
                            /**
                             * Discarding first items will make header item to come into viewport,
                             * thus it should be covered with toolbar, which could be hidden, so show it.
                             */
                            if (position <= 1) {
                                toolbarWidget?.show()  // show toolbar on discard, if was hidden
                            }
                        }
                        .autoDisposable(localScopeProvider)
                        .subscribe({ _ ->
                            val newItems = getVisibleItems(profileId)  // record ids of whatever items are visible after remove
                            val newIds = newItems.map { it.id }
                            DebugLogUtil.d("Discard item ${profileId.substring(0..3)}, visible AFTER[${newIds.size}]: ${newIds.joinToString { it.substring(0..3) }}")
                            checkForNewlyVisibleItems(prevIds, newIds, excludedId = profileId)
                            vm.onSettleVisibleItemsAfterDiscard(newItems)
                        }, DebugLogUtil::e)

                    feedAdapter.remove { it.id == profileId }
                }
                vm.onDiscardProfile(profileId)
            }
            ?.second  // return model and ignore position

    private fun onDiscardProfileRef(profileId: String) {
        onDiscardProfile(profileId)
    }

    protected fun onDiscardMultipleProfilesState(profileIds: Collection<String>) {
//        val prevIds = getVisibleItemIds()  // record ids of visible items before remove
//        with (feedAdapter) {
//            val obs = if (profileIds.intersect(prevIds).isEmpty()) removeSubject
//                      else rv_items.itemAnimator.let { it as FeedItemAnimator }.removeAnimationSubject
//
//            obs.take(1)
//                .doOnSubscribe { localScopeProvider.start() }
//                .doFinally { localScopeProvider.stop() }
//                .autoDisposable(localScopeProvider)
//                .subscribe({
//                    val newIds = getVisibleItemIds()  // record ids of whatever items are visible after remove
//                    checkForNewlyVisibleItems(prevIds, newIds)
//                }, DebugLogUtil::e)
//
//            remove { it.id in profileIds }
//        }
    }

    override fun onEmptyLabelClick() {
        // click on empty screen label should open filters popup
        filtersPopupWidget?.show()
    }

    protected open fun onNoImagesInUserProfile(redirectBackOnFeedScreen: Boolean) {
        val extras = if (redirectBackOnFeedScreen) "&tabExtras={\"backOnFeed\":\"${vm.getFeedName()}\"}" else ""

        Dialogs.showTextDialog(activity,
            descriptionResId = getAddPhotoDialogDescriptionResId(),
            positiveBtnLabelResId = OriginR_string.button_add_photo,
            negativeBtnLabelResId = OriginR_string.button_later,
            positiveListener = { _, _ -> navigate(this@FeedFragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}$extras") },
            negativeListener = { dialog, _ -> vm.onCancelNoImagesInUserProfileDialog(); dialog.dismiss() },
            isCancellable = false)

        showLoading(isVisible = false)
    }

    private fun showLoading(isVisible: Boolean) {
        swipe_refresh_layout
            ?.takeIf { isVisible != it.isRefreshing }  // change visibility w/o interruption of animation, if any
            ?.let { it.isRefreshing = isVisible }
    }

    // --------------------------------------------------------------------------------------------
    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        filtersPopupWidget?.hide()
    }

    override fun onTabTransaction(payload: String?, extras: String?) {
        super.onTabTransaction(payload, extras)
        toolbarWidget?.show()  // switch back on any Feed should show toolbar, if it was hide
        /**
         * If user has intended to like someone's profile (feed item) and had no images in her Profile,
         * such intention is memorized. Next time user navigates on Explore screen, such intention should
         * be fulfilled in case it was actually interrupted by asking to add image on Profile.
         */
        if (isViewModelInitialized) {
            vm.doPendingLikeInAny()
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = createFeedAdapter().apply {
            onBeforeLikeListener = { vm.onBeforeLike(position = it) }
            onImageTouchListener = { x, y -> vm.onImageTouch(x, y) }
            onScrollHorizontalListener = {
                showRefreshPopup(isVisible = false)
                if (totalScrollDown >= AppRes.FEED_TOOLBAR_HEIGHT) {
                    toolbarWidget?.hide()
                }
            }
            settingsClickListener = { model: FeedItemVO, position: Int, positionOfImage: Int ->
                vm.onSettingsClick(profileId = model.id)
                val image = model.images[positionOfImage]
                val payload = FeedItemContextMenuPayload(
                    profileImageUri = image.uri,
                    profileThumbnailUri = image.thumbnailUri,
                    socialInstagram = model.instagram(),
                    socialTiktok = model.tiktok())
                navigate(this@FeedFragment, path = "/feed_item_context_menu?position=$position&profileId=${model.id}&imageId=${image.id}&actions=${contextMenuActions()}&excludedReasons=10,50,70&payload=${payload.toJson()}", rc = RequestCode.RC_CONTEXT_MENU_FEED_ITEM)
            }
            insertItemsSource()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { DebugLogUtil.d2("Inserted ${it.second} feed items at position ${it.first} [${this@FeedFragment.javaClass.simpleName}]") }
                .autoDisposable(scopeProvider)
                .subscribe({ trackVisibilityOnListInsert() }, DebugLogUtil::e)
        }
        invalidateScrollCaches()
        timeKeeper.registerCallback { runOnUiThread { context?.toast(OriginR_string.time_keeper_interval_alert_load) } }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_CONTEXT_MENU_FEED_ITEM -> {
                if (data == null) {
                    Report.w("No output from FeedItemContextMenu dialog")
                    return
                }

                if (resultCode == Activity.RESULT_OK) {
                    val imageId = data.extras!!.getString("imageId")!!
                    val profileId = data.extras!!.getString("profileId")!!
                    val position = data.extras!!.getString("position")!!.toInt()

                    // send like from context menu
                    if (data.hasExtra(Extras.OUT_EXTRA_LIKE_SENT)) {
                        data.getBooleanExtra(Extras.OUT_EXTRA_LIKE_SENT, false)
                            .takeIf { it && vm.onBeforeLike(position) }
                            ?.let {
                                rv_items.linearLayoutManager()?.findViewByPosition(position)
                                    ?.let { vm.onImageTouch(it.pivotX, it.pivotY) }  // show like visual effect
                                vm.onLike(profileId = profileId, imageId = imageId)
                            }
                    }

                    // block / report from context menu
                    if (data.hasExtra(Extras.OUT_EXTRA_REPORT_REASON)) {
                        data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0).let {
                            when (it) {
                                0 -> vm.onBlock(profileId = profileId, imageId = imageId)
                                else -> vm.onReport(profileId = profileId, imageId = imageId, reasonNumber = it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fun onBeforeRefresh() {
            // purge feed on refresh, before fetching a new one
            onClearState(mode = ViewState.CLEAR.MODE_DEFAULT)  // purge Feed while refreshing by state
            showLoading(isVisible = true)
        }

        // --------------------------------------
        super.onActivityCreated(savedInstanceState)
        feedTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewVertical), onError = Consumer(DebugLogUtil::e))
        imagesTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewHorizontal), onError = Consumer(DebugLogUtil::e))
        feedAdapter.trackingBus = imagesTrackingBus
        observeOneShot(vm.discardProfileOneShot(), ::onDiscardProfileRef)
        observeOneShot(vm.likeProfileOneShot()) { feedAdapter.performClickOnLikeButtonAtPosition(rv_items, position = it) }
        observeOneShot(vm.needShowFiltersOneShot()) { filtersPopupWidget?.show() }
        observeOneShot(vm.noImagesInUserProfileOneShot(), ::onNoImagesInUserProfile)
        observeOneShot(vm.notifyOnFeedLoadFinishOneShot()) { timeKeeper.stop() }
        observeOneShot(vm.refreshOneShot()) {
            onBeforeRefresh()
            onRefresh()
        }
        observeOneShot(vm.refreshOnLocationPermissionOneShot()) {
            onBeforeRefresh()
            onRefresh(askForLocation = false)
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun onExpandFilters() {
            toolbarWidget?.hide(animated = false)  // hide toolbar on show filters popup
            if (isAdded) {
                Timber.v("Expand filters")
                childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)?.userVisibleHint = true
            }
        }

        fun onHideFilters() {
            toolbarWidget?.show(animated = false)  // show toolbar on hide filters popup
            if (isAdded) {
                Timber.v("Hide filters")
                childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)?.userVisibleHint = false
            }
        }

        @DebugOnly
        fun onDebugOptionSelect() {
            navigate(this@FeedFragment, path = "/rate_us")
        }

        // --------------------------------------
        super.onViewCreated(view, savedInstanceState)
        filtersPopupWidget = FiltersPopupWidget(view) {
            onShowFiltersPopup()
            childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)
                ?.let { it as? BaseFiltersFragment<*> }
                ?.requestFiltersForUpdate()
        }
        .apply {
            setOnSlideUpListener(object : FiltersPopupWidget.OnSlideUpListener {
                override fun onSlideUp(isSlidingUp: Boolean) {
                    if (isSlidingUp) {
                        onHideFilters()
                    } else {
                        onExpandFilters()
                    }
                }
            })
            setOnStateChangedListener { newState ->
                when (newState) {
                    TopSheetBehavior.STATE_EXPANDED -> onExpandFilters()
                    TopSheetBehavior.STATE_COLLAPSED,
                    TopSheetBehavior.STATE_HIDDEN -> onHideFilters()
                    // TopSheetBehavior.STATE_DRAGGING and
                    // TopSheetBehavior.STATE_SETTLING handled internally
                }
            }
        }
        toolbarWidget = ToolbarWidget(view).init { toolbar ->
            with (toolbar) {
                setTitle(getToolbarTitleResId())
                inflateMenu(if (BuildConfig.IS_STAGING) R.menu.feed_toolbar_menu_debug else R.menu.feed_toolbar_menu)
                itemClicks().filter { toolbarWidget?.isShow() == true }.compose(clickDebounce()).subscribe {
                    when (it.itemId) {
                        R.id.debug -> onDebugOptionSelect()
                        R.id.filters -> filtersPopupWidget?.show()
                    }
                }
                touches { toolbarWidget?.isShow() == true && it.actionMasked == MotionEvent.ACTION_UP }.compose(clickDebounce()).subscribe { filtersPopupWidget?.show() }
            }
        }

        with (rv_items) {
            adapter = feedAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = FeedItemAnimator()
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(OriginListAdapter.VIEW_TYPE_NORMAL, 10)
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            addOnScrollListener(scrollListener)
            addOnScrollListener(itemOffsetScrollListener)
            addOnScrollListener(visibilityTrackingScrollListener)
        }
        with (swipe_refresh_layout) {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setProgressViewEndTarget(false, resources.getDimensionPixelSize(R.dimen.feed_swipe_refresh_layout_spinner_end_offset))
            refreshes().compose(clickDebounce()).subscribe { onRefreshGesture() }
            _swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }

        // top sheet
        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_content, createFiltersFragment(), BaseFiltersFragment.TAG)
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        toolbarWidget?.show()  // show toolbar on start
    }

    override fun onResume() {
        super.onResume()
        feedTrackingBus.subscribe()
        imagesTrackingBus.subscribe()
    }

    override fun onPause() {
        super.onPause()
        feedTrackingBus.unsubscribe()
        imagesTrackingBus.unsubscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with (rv_items) {
            removeOnScrollListener(scrollListener)
            removeOnScrollListener(itemOffsetScrollListener)
            removeOnScrollListener(visibilityTrackingScrollListener)
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}

                override fun onViewDetachedFromWindow(v: View) {
                    this@with.adapter = null
                }
            })
            adapter = null
            layoutManager = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeKeeper.unregisterCallback()
        feedAdapter.dispose()
    }

    // --------------------------------------------------------------------------------------------
    protected open fun onRefreshGesture() {
        DebugLogUtil.i("PULL to REFRESH")
        // purge feed on refresh, before fetching a new one
        onClearState(mode = ViewState.CLEAR.MODE_DEFAULT)  // purge Feed on manual refresh
        onRefresh()  // should be the last action in subclasses, if overridden
    }

    private fun onRefresh(askForLocation: Boolean = true): Boolean =
        if (!connectionManager.isNetworkAvailable()) {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // no connection on refresh
            noConnection(this@FeedFragment)
            false
        } else {
            feedTrackingBus.allowSingleUnchanged()
            invalidateScrollCaches()
            if (askForLocation) {
                /**
                 * Asks for location permission, and if granted - callback will then handle
                 * to call refreshing procedure.
                 */
                if (!permissionManager.askForLocationPermission(this@FeedFragment)) {
                    onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // no location permission on refresh
                }
            } else {
                timeKeeper.start()  // refresh has just started
                vm.onRefresh()  // refresh without asking for location permission
            }
            true
        }

    protected fun showRefreshPopup(isVisible: Boolean) {
        btn_refresh_popup.changeVisibility(isVisible = isVisible && vm.isRefreshOnPush())
    }

    protected open fun onShowFiltersPopup() {
        // override in subclasses
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    private var instantTotalScrollDown: Int = 0
    private var instantTotalScrollUp: Int = 0
    private var totalScrollDown: Int = 0
    private var headerView: View? = null

    private fun clearScrollData() {
        instantTotalScrollDown = 0
        instantTotalScrollUp = 0
        totalScrollDown = 0
        headerView = null
    }

    private fun headerView(): View? {
        if (headerView == null) {
            headerView = rv_items.linearLayoutManager()?.findViewByPosition(0)
        }
        return headerView
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun deltaTop(): Int = headerView()?.bottom ?: 0

            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                if (dy > 0) {  // scroll list down - to see new items
                    if (btn_refresh_popup.isVisible()) {
                        showRefreshPopup(isVisible = false)
                    }
                    instantTotalScrollUp = 0
                    instantTotalScrollDown += dy
                    totalScrollDown += dy
                    if (instantTotalScrollDown >= AppRes.FEED_TOOLBAR_HEIGHT) {
                        toolbarWidget?.hide()  // hide toolbar on scroll
                    }
                    true
                } else if (dy < 0) {  // scroll list up - to see previous items
                    if (!btn_refresh_popup.isVisible()) {
                        showRefreshPopup(isVisible = true)
                    }
                    instantTotalScrollDown = 0
                    instantTotalScrollUp -= dy
                    totalScrollDown += dy  // negative value decreases total scroll
                    if (instantTotalScrollUp >= AppRes.FEED_TOOLBAR_HEIGHT || deltaTop() >= 12) {
                        toolbarWidget?.show()  // show toolbar on scroll
                    }
                    true
                } else {
                    // dy == 0 is a layout change, not a user scroll, ignore
                    false
                }
            }
        }
    }

    // ------------------------------------------
    private lateinit var offsetScrollStrats: List<OffsetScrollStrategy>

    private fun invalidateScrollCaches() {
        offsetScrollStrats = getOffsetScrollStrategies()
    }

    protected fun getStrategyByTag(tag: String): OffsetScrollStrategy? = offsetScrollStrats.find { it.tag == tag }

    protected open fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        listOf(OffsetScrollStrategy(tag = "dot tabs bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_BOTTOM2, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll),
               OffsetScrollStrategy(tag = "footer", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_FOOTER_LABEL_BOTTOM, hide = FeedFooterViewHolderHideControls, show = FeedFooterViewHolderShowControls),
               OffsetScrollStrategy(tag = "online bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_ONLINE_STATUS_BOTTOM, hide = FeedViewHolderHideOnlineStatusOnScroll, show = FeedViewHolderShowOnlineStatusOnScroll),
               OffsetScrollStrategy(tag = "settings bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_BOTTOM, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll),
               OffsetScrollStrategy(tag = "prop about bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_ABOUT_BOTTOM, hide = FeedViewHolderHideAboutOnScroll, show = FeedViewHolderShowAboutOnScroll),
               OffsetScrollStrategy(tag = "prop status bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_STATUS_BOTTOM, hide = FeedViewHolderHideStatusOnScroll, show = FeedViewHolderShowStatusOnScroll),
               OffsetScrollStrategy(tag = "prop 0 bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_BOTTOM_0, hide = FeedViewHolderHideOnScroll(1), show = FeedViewHolderShowOnScroll(1)),
               OffsetScrollStrategy(tag = "prop 1 bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_BOTTOM_1, hide = FeedViewHolderHideOnScroll(0), show = FeedViewHolderShowOnScroll(0)),
               OffsetScrollStrategy(tag = "prop name 0 bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_BOTTOM_0, hide = FeedViewHolderHideNameOnScroll(0, type = OffsetScrollStrategy.Type.BOTTOM), show = FeedViewHolderShowNameOnScroll(0, type = OffsetScrollStrategy.Type.BOTTOM)),
               OffsetScrollStrategy(tag = "prop name 1 bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_BOTTOM_1, hide = FeedViewHolderHideNameOnScroll(1, type = OffsetScrollStrategy.Type.BOTTOM), show = FeedViewHolderShowNameOnScroll(1, type = OffsetScrollStrategy.Type.BOTTOM)),
               OffsetScrollStrategy(tag = "prop name 2 bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_BOTTOM_2, hide = FeedViewHolderHideNameOnScroll(2, type = OffsetScrollStrategy.Type.BOTTOM), show = FeedViewHolderShowNameOnScroll(2, type = OffsetScrollStrategy.Type.BOTTOM)))

    private val itemOffsetScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun trackScrollOffset(rv: RecyclerView) {
                rv.linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    val top = getTopBorderForOffsetScroll()
                    val bottom = getBottomBorderForOffsetScroll()
                    for (i in from..to) {
                        processItemView(i, it.findViewByPosition(i), top, bottom)
                    }
                }
            }

            super.onScrolled(rv, dx, dy)
            trackScrollOffset(rv)
        }
    }

    // helper method
    private fun processItemViewControlVisibility(position: Int, view: View, top: Int, bottom: Int) {
        fun handleBottomStrategy(it: OffsetScrollStrategy) {
            if (bottom - view.top <= it.deltaOffset) {
                if (!it.isHiddenAtAndSync(position)) {
                    feedAdapter.notifyItemChanged(position, it.hide)
                }
            } else {
                if (!it.isShownAtAndSync(position)) {
                    feedAdapter.notifyItemChanged(position, it.show)
                }
            }
        }

        fun handleTopStrategy(it: OffsetScrollStrategy) {
            if (top - view.top >= it.deltaOffset) {
                if (!it.isHiddenAtAndSync(position)) {
                    feedAdapter.notifyItemChanged(position, it.hide)
                }
            } else {
                if (!it.isShownAtAndSync(position)) {
                    feedAdapter.notifyItemChanged(position, it.show)
                }
            }
        }

        fun handleStrategy(it: OffsetScrollStrategy) =
            when (it.type) {
                OffsetScrollStrategy.Type.BOTTOM -> handleBottomStrategy(it)
                OffsetScrollStrategy.Type.TOP -> handleTopStrategy(it)
            }

        // --------------------------------------
        view.post {  // avoid change rv during layout, leading to crash
            val targets = mutableSetOf<Int>()
            if (view.top >= top && view.bottom <= bottom) {
                // view is completely fits the space between (top, bottom), so all strategies must 'show'
                offsetScrollStrats.forEach {
                    if (targets.add(it.target())) {  // ignore strategy with already affected target
                        if (!it.isShownAtAndSync(position)) {
                            feedAdapter.notifyItemChanged(position, it.show)
                        }
                    }
                }
            } else if (view.bottom <= top || view.top >= bottom) {
                // view is completely hidden below bottom or above top boundaries, so all strategies must 'hide'
                offsetScrollStrats.forEach {
                    if (targets.add(it.target())) {  // ignore strategy with already affected target
                        if (!it.isHiddenAtAndSync(position)) {
                            feedAdapter.notifyItemChanged(position, it.hide)
                        }
                    }
                }
            } else if (view.bottom > bottom && view.top >= top) {
                // view is completely below top border and partially below bottom border,
                // so only bottom strategies must operate, all top ones must be ignored (or 'show')
                offsetScrollStrats.forEach {
                    when (it.type) {
                        OffsetScrollStrategy.Type.BOTTOM -> {
                            if (targets.add(it.target())) {  // ignore strategy with already affected target
                                handleBottomStrategy(it)
                            }
                        }
                        OffsetScrollStrategy.Type.TOP -> { /* ignored */ }
                    }
                }
            } else if (view.top < top && view.bottom <= bottom) {
                // view is completely above bottom border and partially above top border,
                // so only top strategies must operate, all bottom ones must be ignored (or 'show')
                offsetScrollStrats.forEach {
                    when (it.type) {
                        OffsetScrollStrategy.Type.BOTTOM -> { /* ignored */ }
                        OffsetScrollStrategy.Type.TOP -> {
                            if (targets.add(it.target())) {  // ignore strategy with already affected target
                                handleTopStrategy(it)
                            }
                        }
                    }
                }
            } else {
                // view is partially clipped with both of boundaries (top, bottom)
                offsetScrollStrats.forEach {
                    if (targets.add(it.target())) {  // if target hasn't been affected yet - apply strategy on it
                        handleStrategy(it)
                    } else {
                        // target has been affected already by some previous strategy,
                        // so need to decide whether that strategy should be overridden by this strategy
                        when (it.type) {
                            OffsetScrollStrategy.Type.BOTTOM -> {
                                if (view.top + it.deltaOffset >= bottom) {
                                    handleBottomStrategy(it)
                                }
                            }
                            OffsetScrollStrategy.Type.TOP -> {
                                if (view.top + it.deltaOffset <= top) {
                                    handleTopStrategy(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected open fun getBottomBorderForOffsetScroll(): Int = rv_items.bottom - AppRes.MAIN_BOTTOM_BAR_HEIGHT
    protected open fun getTopBorderForOffsetScroll(): Int = -2000000000  // no top border by default

    // helper method
    private fun processItemView(position: Int, view: View?, top: Int, bottom: Int) {
        view?.let { processItemViewControlVisibility(position, view, top, bottom) }
    }

    /**
     * Directly applies offset scroll strategies on item at [position], if any,
     * depending on scroll offset of related view in [rv_items].
     */
    protected fun trackScrollOffsetForPosition(position: Int) {
        /**
         * Loop over all offset scroll strategies and remove [position] from their lists of affected
         * positions to force strategies to be applied on item at [position] again.
         */
        offsetScrollStrats.forEach { it.forgetPosition(position) }
        // apply strategies on item at position, regardless of whether that position has been affected before
        rv_items.linearLayoutManager()?.let {
            val top = getTopBorderForOffsetScroll()
            val bottom = getBottomBorderForOffsetScroll()
            processItemView(position, it.findViewByPosition(position), top, bottom)
        }
    }

    // ------------------------------------------
    private val visibilityTrackingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            if (dy != 0) {
                trackVisibility(rv)
            }
        }
    }

    /**
     * Detects which items are currently visible while feed is scrolling vertically.
     */
    private fun trackVisibility(rv: RecyclerView?) {
        rv?.linearLayoutManager()?.let {
            val from = it.findFirstVisibleItemPosition()
            val to = it.findLastVisibleItemPosition()
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                return  // avoid internal inconsistent calls while RecyclerView is adjusting
            }

            val items = feedAdapter.getItemsExposed(from = from, to = to)
            try {
                // use 0th image, because cannot access currently visible image on feed item, see [FeedViewModel::onViewVertical] for more info
                var range = EqualRange(from = from, to = to,
                    items = items.map { feedItem ->
                        val image = if (feedItem.isRealModel && feedItem.images.isNotEmpty()) feedItem.images[0]
                                    else EmptyImage
                        ProfileImageVO(profileId = feedItem.id, image = image)
                    })

                range = range.takeIf { feedAdapter.withHeader() }
                    ?.takeIf { from == 0 }
                    ?.dropItems(n = 1)  // exclude header item from visibility tracking
                    ?: range  // no header item within the visible range

                range = range.takeIf { feedAdapter.withFooter() }
                    ?.takeIf { to == feedAdapter.footerPosition() }
                    ?.dropLastItems(n = 1)  // exclude footer item from visibility tracking
                    ?: range  // no footer item within the visible range

                Timber.v("Visible feed items [${range.size}] [${range.from}, ${range.to}]: $range")
                feedTrackingBus.postViewEvent(range)

            } catch (e: IllegalArgumentException) {
                Report.capture(e, message = e.message, tag = "trackVisibility",
                               extras = listOf("from" to "$from", "to" to "$to", "size" to "${items.size}"))
            }
        }
    }

    private fun trackVisibilityOnListInsert() {
        trackVisibility(rv_items)
    }
}
