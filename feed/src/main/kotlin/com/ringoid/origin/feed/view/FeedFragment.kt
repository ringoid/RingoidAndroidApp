package com.ringoid.origin.feed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.techisfun.android.topsheet.TopSheetBehavior
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
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
import com.ringoid.origin.model.BlockReportPayload
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import com.ringoid.origin.view.base.BaseListFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.IEmptyScreenCallback
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.isVisible
import com.ringoid.utility.linearLayoutManager
import com.ringoid.widget.view.swipes
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<VM : FeedViewModel> : BaseListFragment<VM>(), IEmptyScreenCallback {

    protected lateinit var feedAdapter: BaseFeedAdapter
        private set
    private lateinit var feedTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>
    private lateinit var imagesTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    protected var filtersPopupWidget: FiltersPopupWidget? = null
    protected var toolbarWidget: ToolbarWidget? = null

    // ------------------------------------------
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
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> onClearState(mode = newState.mode)
            is ViewState.DONE -> {
                when (newState.residual) {
                    is ASK_TO_ENABLE_LOCATION_SERVICE -> showLoading(isVisible = false)
                    is DISCARD_PROFILE -> onDiscardProfileState(profileId = (newState.residual as DISCARD_PROFILE).profileId)
                    is NO_IMAGES_IN_USER_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = getAddPhotoDialogDescriptionResId(),
                            positiveBtnLabelResId = OriginR_string.button_add_photo,
                            negativeBtnLabelResId = OriginR_string.button_later,
                            positiveListener = { _, _ -> navigate(this@FeedFragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}") })

                        showLoading(isVisible = false)
                    }
                    is REFRESH -> {
                        // purge feed on refresh, before fetching a new one
                        onClearState(mode = ViewState.CLEAR.MODE_DEFAULT)
                        showLoading(isVisible = true)
                        onRefresh()
                    }
                }
            }
            is ViewState.IDLE -> {
                fl_empty_container?.changeVisibility(isVisible = false)
                showLoading(isVisible = false)
            }
            is ViewState.LOADING -> showLoading(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
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
        toolbarWidget?.removeScrollFlags()  // prevent toolbar from scrolling while in CLEAR state
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

    protected open fun onDiscardProfileState(profileId: String): FeedItemVO? =
        feedAdapter.findModel { it.id == profileId }
            ?.also { _ ->
                val count = feedAdapter.getModelsCount()
                if (count <= 1) {  // remove last feed item - show empty stub directly
                    onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)
                } else {  // remove not last feed item
                    val prevIds = getVisibleItemIds(profileId)  // record ids of visible items before remove
                    DebugLogUtil.d("Discard item ${profileId.substring(0..3)}, visible BEFORE[${prevIds.size}]: ${prevIds.joinToString { it.substring(0..3) }}")

                    /**
                     * After finishing item remove animation, detect what items come into viewport
                     * and call [FeedViewModel.onItemBecomeVisible] on each of them.
                     */
                    rv_items.itemAnimator
                        .let { it as FeedItemAnimator }
                        .removeAnimationSubject
                        .take(1)  // single-shot subscription
                        .doOnSubscribe { localScopeProvider.start() }
                        .doOnDispose { DebugLogUtil.v("Discard item ${profileId.substring(0..3)}: disposed local subscription") }
                        .doFinally {
                            DebugLogUtil.v("Discard item ${profileId.substring(0..3)} has completed")
                            localScopeProvider.stop()
                        }
                        .autoDisposable(localScopeProvider)
                        .subscribe({ _ ->
                            val newItems = getVisibleItems(profileId)  // record ids of whatever items are visible after remove
                            val newIds = newItems.map { it.id }
                            DebugLogUtil.d("Discard item ${profileId.substring(0..3)}, visible AFTER[${newIds.size}]: ${newIds.joinToString { it.substring(0..3) }}")
                            checkForNewlyVisibleItems(prevIds, newIds, excludedId = profileId)
                            vm.onSettleVisibleItemsAfterDiscard(newItems)
                        }, Timber::e)

                    feedAdapter.remove { it.id == profileId }
                }
                vm.onDiscardProfile(profileId)
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
//                }, Timber::e)
//
//            remove { it.id in profileIds }
//        }
    }

    override fun onEmptyLabelClick() {
        // click on empty screen label should open filters popup
        filtersPopupWidget?.show()
    }

    internal fun showLoading(isVisible: Boolean) {
        swipe_refresh_layout
            ?.takeIf { isVisible != it.isRefreshing }  // change visibility w/o interruption of animation, if any
            ?.let { it.isRefreshing = isVisible }
    }

    // --------------------------------------------------------------------------------------------
    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        filtersPopupWidget?.hide()
    }

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        toolbarWidget?.show(isVisible = true)  // switch back on any Feed should show toolbar, if was hide
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = createFeedAdapter().apply {
            onBeforeLikeListener = { vm.onBeforeLike() }
            onImageTouchListener = { x, y -> vm.onImageTouch(x, y) }
            onScrollHorizontalListener = {
                showRefreshPopup(isVisible = false)
                toolbarWidget?.show(isVisible = false)
            }
            settingsClickListener = { model: FeedItemVO, position: Int, positionOfImage: Int ->
                vm.onSettingsClick(model.id)
                val image = model.images[positionOfImage]
                val payload = BlockReportPayload(
                    profileImageUri = image.uri,
                    profileThumbnailUri = image.thumbnailUri
                )
                navigate(this@FeedFragment, path = "/block_dialog?position=$position&profileId=${model.id}&imageId=${image.id}&excludedReasons=10,50,70&payload=${payload.toJson()}", rc = RequestCode.RC_BLOCK_DIALOG)
            }
        }
        invalidateScrollCaches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (data == null) {
                    SentryUtil.w("No output from Block/Report dialog")
                    return
                }

                if (resultCode == Activity.RESULT_OK) {
                    val imageId = data.extras!!.getString("imageId")!!
                    val profileId = data.extras!!.getString("profileId")!!

                    if (data.hasExtra(Extras.OUT_EXTRA_REPORT_REASON)) {
                        val reasonNumber = data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0)
                        vm.onReport(profileId = profileId, imageId = imageId, reasonNumber = reasonNumber)
                    } else {
                        vm.onBlock(profileId = profileId, imageId = imageId)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        feedTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewVertical), onError = Consumer(Timber::e))
        imagesTrackingBus = TrackingBus(onSuccess = Consumer(vm::onViewHorizontal), onError = Consumer(Timber::e))
        feedAdapter.trackingBus = imagesTrackingBus
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fun onExpandFilters() {
            toolbarWidget?.collapse()
            overlay.changeVisibility(isVisible = true)
            childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)?.userVisibleHint = true
        }

        fun onHideFilters() {
            toolbarWidget?.expand()
            overlay.changeVisibility(isVisible = false)
            childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)?.userVisibleHint = false
        }

        super.onViewCreated(view, savedInstanceState)
        filtersPopupWidget = FiltersPopupWidget(view) {
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
                inflateMenu(R.menu.feed_toolbar_menu)
                setOnClickListener { filtersPopupWidget?.show() }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.filters -> { filtersPopupWidget?.show(); true }
                        else -> false
                    }
                }
            }
        }

        with (rv_items) {
            adapter = feedAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = FeedItemAnimator()
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(OriginListAdapter.VIEW_TYPE_NORMAL, 10)
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            addOnScrollListener(itemOffsetScrollListener)
            addOnScrollListener(topScrollListener)
            addOnScrollListener(visibilityTrackingScrollListener)
        }
        with (swipe_refresh_layout) {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setProgressViewEndTarget(false, resources.getDimensionPixelSize(R.dimen.feed_swipe_refresh_layout_spinner_end_offset))
            refreshes().compose(clickDebounce()).subscribe { onRefreshGesture() }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }

        // top sheet
        with (overlay) {
            setOnTouchListener { _, _ -> filtersPopupWidget?.hide(); true }
        }
        if (savedInstanceState == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fl_content, createFiltersFragment(), BaseFiltersFragment.TAG)
                .commitNow()
        }
    }

    override fun onStart() {
        super.onStart()
        toolbarWidget?.show(isVisible = true)
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
            removeOnScrollListener(itemOffsetScrollListener)
            removeOnScrollListener(topScrollListener)
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
        feedAdapter.dispose()
    }

    // --------------------------------------------------------------------------------------------
    protected open fun onRefreshGesture() {
        // purge feed on refresh, before fetching a new one
        onClearState(mode = ViewState.CLEAR.MODE_DEFAULT)
        onRefresh()  // should be the last action in subclasses, if overridden
    }

    private fun onRefresh(): Boolean =
        if (!connectionManager.isNetworkAvailable()) {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
            noConnection(this@FeedFragment)
            false
        } else {
            feedTrackingBus.allowSingleUnchanged()
            invalidateScrollCaches()
            /**
             * Asks for location permission, and if granted - callback will then handle
             * to call refreshing procedure.
             */
            if (!permissionManager.askForLocationPermission(this@FeedFragment)) {
                onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
            }
            true
        }

    protected fun showRefreshPopup(isVisible: Boolean) {
        btn_refresh_popup.changeVisibility(isVisible = isVisible && vm.refreshOnPush.value == true)
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    private val topScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                if (dy > 0) {  // scroll list down - to see new items
                    if (btn_refresh_popup.isVisible()) {
                        showRefreshPopup(isVisible = false)
                    }
                } else {  // scroll list up - to see previous items
                    if (!btn_refresh_popup.isVisible()) {
                        showRefreshPopup(isVisible = true)
                    }
                }
            }
        }
    }

    // ------------------------------------------
    private lateinit var offsetScrollStrats: List<OffsetScrollStrategy>

    protected fun invalidateScrollCaches() {
        offsetScrollStrats = getOffsetScrollStrategies()
    }

    protected fun getStrategyByTag(tag: String): OffsetScrollStrategy? = offsetScrollStrats.find { it.tag == tag }

    protected open fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        listOf(OffsetScrollStrategy(tag = "dot tabs bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_BOTTOM2, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll),
               OffsetScrollStrategy(tag = "footer", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_FOOTER_LABEL_BOTTOM, hide = FeedFooterViewHolderHideControls, show = FeedFooterViewHolderShowControls),
               OffsetScrollStrategy(tag = "online bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_ONLINE_STATUS_BOTTOM, hide = FeedViewHolderHideOnlineStatusOnScroll, show = FeedViewHolderShowOnlineStatusOnScroll),
               OffsetScrollStrategy(tag = "settings bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_BOTTOM, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll),
               OffsetScrollStrategy(tag = "prop about bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_PROPERTY_ABOUT_BOTTOM, hide = FeedViewHolderHideAboutOnScroll, show = FeedViewHolderShowAboutOnScroll),
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
                    val bottom = getBottomBorderForOffsetScroll() - (if (dy < 0) AppRes.FEED_TOOLBAR_HEIGHT else 0)
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
            val bottom = getBottomBorderForOffsetScroll() - (if (toolbarWidget?.isShow() == true) AppRes.FEED_TOOLBAR_HEIGHT else 0)
            processItemView(position, it.findViewByPosition(position), top, bottom)
        }
    }

    // ------------------------------------------
    private val visibilityTrackingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun trackVisibility(rv: RecyclerView) {
                rv.linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
                        return  // avoid internal inconsistent calls while RecyclerView is adjusting
                    }

                    val items = feedAdapter.getItemsExposed(from = from, to = to)
                    // use 0th image, because cannot access currently visible image on feed item, see [FeedViewModel::onViewVertical] for more info
                    var range = EqualRange(from = from, to = to,
                        items = items.map {
                            val image = if (it.isRealModel && it.images.isNotEmpty()) it.images[0] else EmptyImage
                            ProfileImageVO(profileId = it.id, image = image)
                        })
                    range = range.takeIf { feedAdapter.withHeader() }
                        ?.takeIf { from == 0 }
                        ?.let { it.dropItems(n = 1) }  // exclude header item from visibility tracking
                            ?: range  // no header item within the visible range
                    range = range.takeIf { feedAdapter.withFooter() }
                        ?.takeIf { to == feedAdapter.footerPosition() }
                        ?.let { it.dropLastItems(n = 1) }  // exclude footer item from visibility tracking
                            ?: range  // no footer item within the visible range
                    Timber.v("Visible feed items [${range.size}] [${range.from}, ${range.to}]: $range")
                    feedTrackingBus.postViewEvent(range)
                }
            }

            super.onScrolled(rv, dx, dy)
            trackVisibility(rv)
        }
    }
}
