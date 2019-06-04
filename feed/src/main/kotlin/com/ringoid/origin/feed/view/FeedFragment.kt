package com.ringoid.origin.feed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.base.view.ViewState
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
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import com.ringoid.origin.view.base.BaseListFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.*
import com.ringoid.utility.collection.EqualRange
import com.ringoid.widget.view.swipes
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<VM : FeedViewModel> : BaseListFragment<VM>() {

    protected lateinit var feedAdapter: BaseFeedAdapter
        private set
    private lateinit var feedTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>
    private lateinit var imagesTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    override fun getLayoutId(): Int = R.layout.fragment_feed
    override fun getRecyclerView(): RecyclerView = rv_items

    protected abstract fun createFeedAdapter(): BaseFeedAdapter
    @StringRes protected abstract fun getAddPhotoDialogDescriptionResId(): Int

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
                    is NO_IMAGES_IN_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = getAddPhotoDialogDescriptionResId(),
                            positiveBtnLabelResId = OriginR_string.button_add_photo,
                            negativeBtnLabelResId = OriginR_string.button_later,
                            positiveListener = { _, _ -> navigate(this@FeedFragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}") })
                        showLoading(isVisible = false)
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> showLoading(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
        }
    }

    private fun onClearState(mode: Int) {
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
        vm.onClearScreen()
        getEmptyStateInput(mode)?.let {
            showEmptyStub(input = it)
            showLoading(isVisible = false)
        } ?: run { fl_empty_container?.changeVisibility(isVisible = false) }
    }

    private fun getVisibleItemIds(excludedId: String? = null): List<String> =
        rv_items.linearLayoutManager()?.let { lm ->
            val from = lm.findFirstVisibleItemPosition()
            val to = lm.findLastVisibleItemPosition()
            if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION) {
                feedAdapter.getModelsInRange(from, to)
                    .apply { excludedId?.let { exId -> removeAll { it.id == exId } } }
                    .map { it.id }
            } else emptyList()
        } ?: emptyList()

    private fun checkForNewlyVisibleItems(prevIds: Collection<String>, newIds: Collection<String>, excludedId: String? = null) {
        newIds.toMutableList()
            .also { it.removeAll(prevIds) }
            .also { DebugLogUtil.d("Discarded ${excludedId?.substring(0..3)}, became visible[${it.size}]: ${it.joinToString { it.substring(0..3) }}") }
            .takeIf { it.isNotEmpty() }
            ?.forEach { id ->
                feedAdapter.findModel { it.id == id }
                    ?.let {
                        val imageId = it.images[it.positionOfImage].id
                        vm.onItemBecomeVisible(profileId = it.id, imageId = imageId)
                    }
            }
    }

    protected fun onDiscardProfileState(profileId: String): FeedItemVO? =
        feedAdapter.findModel { it.id == profileId }
            ?.also { _ ->
                val count = feedAdapter.getModelsCount()
                if (count <= 1) {  // remove last feed item - show empty stub directly
                    onClearState(ViewState.CLEAR.MODE_EMPTY_DATA)
                } else {  // remove not last feed item
                    val prevIds = getVisibleItemIds(profileId)  // record ids of visible items before remove
                    DebugLogUtil.v("Discard item ${profileId.substring(0..3)}, visible BEFORE[${prevIds.size}]: ${prevIds.joinToString { it.substring(0..3) }}")

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
                            val newIds = getVisibleItemIds(profileId)  // record ids of whatever items are visible after remove
                            DebugLogUtil.v("Discard item ${profileId.substring(0..3)}, visible AFTER[${newIds.size}]: ${newIds.joinToString { it.substring(0..3) }}")
                            checkForNewlyVisibleItems(prevIds, newIds, excludedId = profileId)
                        }, Timber::e)

                    feedAdapter.remove { it.id == profileId }
                }
                vm.onDiscardProfile(profileId)
            }

    protected fun onDiscardMultipleProfilesState(profileIds: Collection<String>) {
        val prevIds = getVisibleItemIds()  // record ids of visible items before remove
        with (feedAdapter) {
            val obs = if (profileIds.intersect(prevIds).isEmpty()) removeSubject
                      else rv_items.itemAnimator.let { it as FeedItemAnimator }.removeAnimationSubject

            obs.take(1)
                .doOnSubscribe { localScopeProvider.start() }
                .doFinally { localScopeProvider.stop() }
                .autoDisposable(localScopeProvider)
                .subscribe({ _ ->
                    val newIds = getVisibleItemIds()  // record ids of whatever items are visible after remove
                    checkForNewlyVisibleItems(prevIds, newIds)
                }, Timber::e)

            remove { it.id in profileIds }
        }
    }

    private fun onIdleState() {
        showLoading(isVisible = false)
    }

    internal fun showLoading(isVisible: Boolean) {
        swipe_refresh_layout
            ?.takeIf { isVisible != it.isRefreshing }  // change visibility w/o interruption of animation, if any
            ?.let { it.isRefreshing = isVisible }
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
                showScrollFab(isVisible = false)
            }
            settingsClickListener = { model: FeedItemVO, position: Int, positionOfImage: Int ->
                val image = model.images[positionOfImage]
                scrollToTopOfItemAtPositionAndPost(position).post {
                    showRefreshPopup(isVisible = false)
                    showScrollFab(isVisible = false)
                    notifyItemChanged(position, FeedViewHolderHideControls)
                }
                vm.onSettingsClick(model.id)
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                navigate(this@FeedFragment, path = "/block_dialog?position=$position&profileId=${model.id}&imageId=${image.id}&excludedReasons=10,50,70", rc = RequestCode.RC_BLOCK_DIALOG)
            }
        }
        offsetScrollStrats = getOffsetScrollStrategies()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (data == null) {
                    SentryUtil.w("No output from Block/Report dialog")
                    return
                }

                val position = data.extras!!.getString("position", "0").toInt()
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = true)
                scrollToTopOfItemAtPosition(position, offset = AppRes.BUTTON_HEIGHT)
                showRefreshPopup(isVisible = true)
                showScrollFab(isVisible = true, restoreVisibility = true)

                if (resultCode == Activity.RESULT_OK) {
                    val imageId = data.extras!!.getString("imageId")!!
                    val profileId = data.extras!!.getString("profileId")!!

                    if (data.hasExtra(Extras.OUT_EXTRA_REPORT_REASON)) {
                        val reasonNumber = data.getIntExtra(Extras.OUT_EXTRA_REPORT_REASON, 0)
                        vm.onReport(profileId = profileId, imageId = imageId, reasonNumber = reasonNumber)
                    } else {
                        vm.onBlock(profileId = profileId, imageId = imageId)
                    }
                } else {
                    // on dialog dismiss = show controls back
                    getRecyclerView().post { feedAdapter.notifyItemChanged(position, FeedViewHolderShowControls) }
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
        super.onViewCreated(view, savedInstanceState)
        rv_items.apply {
            adapter = feedAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            itemAnimator = FeedItemAnimator { rv_items.post { feedAdapter.notifyItemRangeChanged(it - 1, 2, FeedViewHolderShowControls) } }
            setHasFixedSize(true)
            setItemViewCacheSize(6)
            recycledViewPool.setMaxRecycledViews(OriginListAdapter.VIEW_TYPE_NORMAL, 10)
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            addOnScrollListener(itemOffsetScrollListener)
            addOnScrollListener(topScrollListener)
            addOnScrollListener(visibilityTrackingScrollListener)
        }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setProgressViewEndTarget(false, resources.getDimensionPixelSize(R.dimen.feed_swipe_refresh_layout_spinner_end_offset))
            refreshes().compose(clickDebounce()).subscribe { onRefresh() }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }
        scroll_fab.clicks().compose(clickDebounce()).subscribe {
            showScrollFab(isVisible = false)
            scrollToTopOfItemAtPosition(position = 0)
        }
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
        rv_items.apply {
            removeOnScrollListener(itemOffsetScrollListener)
            removeOnScrollListener(topScrollListener)
            removeOnScrollListener(visibilityTrackingScrollListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        feedAdapter.dispose()
    }

    // --------------------------------------------------------------------------------------------
    protected open fun onRefresh() {
        if (!connectionManager.isNetworkAvailable()) {
            showLoading(isVisible = false)
            noConnection(this@FeedFragment)
        } else {
            feedTrackingBus.allowSingleUnchanged()
            offsetScrollStrats = getOffsetScrollStrategies()
            /**
             * Asks for location permission, and if granted - callback will then handle
             * to call refreshing procedure.
             */
            if (!permissionManager.askForLocationPermission(this@FeedFragment)) {
                onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
            }
        }
    }

    // ------------------------------------------
    private var wasFabVisible: Boolean = false

    protected fun showRefreshPopup(isVisible: Boolean) {
        btn_refresh_popup.changeVisibility(isVisible = isVisible && vm.refreshOnPush.value == true)
    }

    protected fun showScrollFab(isVisible: Boolean, restoreVisibility: Boolean = false) {
        val xIsVisible = if (restoreVisibility) {
            wasFabVisible = scroll_fab.isVisible()
            isVisible && wasFabVisible
        } else isVisible
        scroll_fab.changeVisibility(isVisible = xIsVisible)
    }

    // ------------------------------------------
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        rv_items?.scrollBy(0, -1)
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
                    if (scroll_fab.isVisible()) {
                        showScrollFab(isVisible = false)
                    }
                } else {  // scroll list up - to see previous items
                    val offset = rv.computeVerticalScrollOffset()
                    if (!btn_refresh_popup.isVisible()) {
                        showRefreshPopup(isVisible = true)
                    }
                    if (scroll_fab.isVisible()) {
                        if (offset <= 0) {
                            showScrollFab(isVisible = false)
                        }
                    } else {
                        if (offset > 0) {
                            showScrollFab(isVisible = true)
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------
    private lateinit var offsetScrollStrats: List<OffsetScrollStrategy>

    protected fun getStrategyByTag(tag: String): OffsetScrollStrategy? = offsetScrollStrats.find { it.tag == tag }

    protected open fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        listOf(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.DOWN, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_BOTTOM2, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll),
               OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_ONLINE_STATUS_TOP, hide = FeedViewHolderHideOnlineStatusOnScroll, show = FeedViewHolderShowOnlineStatusOnScroll),
               OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_BOTTOM, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll),
               OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_FOOTER_LABEL_BOTTOM, hide = FeedFooterViewHolderHideControls, show = FeedFooterViewHolderShowControls))

    private val itemOffsetScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            fun processItemViewControlVisibility(position: Int, view: View, top: Int, bottom: Int) {
                offsetScrollStrats.forEach {
                    view.post {
                        // avoid change rv during layout, leading to crash
                        when (it.type) {
                            OffsetScrollStrategy.Type.BOTTOM -> {
                                if (bottom - view.top <= AppRes.FEED_ITEM_MID_BTN_BOTTOM + 4) {
                                    if (bottom - view.top < it.deltaOffset) {
                                        if (!it.isHiddenAtAndSync(position)) {
                                            feedAdapter.notifyItemChanged(position, it.hide)
                                        }
                                    } else {
                                        if (!it.isShownAtAndSync(position)) {
                                            feedAdapter.notifyItemChanged(position, it.show)
                                        }
                                    }
                                }
                            }
                            OffsetScrollStrategy.Type.DOWN -> {
                                if (bottom - view.top < it.deltaOffset) {
                                    if (!it.isHiddenAtAndSync(position)) {
                                        feedAdapter.notifyItemChanged(position, it.hide)
                                    }
                                } else {
                                    if (!it.isShownAtAndSync(position)) {
                                        feedAdapter.notifyItemChanged(position, it.show)
                                    }
                                }
                            }
                            OffsetScrollStrategy.Type.TOP -> {
                                if (view.top - top <= AppRes.FEED_ITEM_MID_BTN_TOP + 4) {
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
                            }
                            OffsetScrollStrategy.Type.UP -> {
                                if (view.top - top <= 0) {
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
                            }
                        }
                    }
                }
            }

            fun processItemView(position: Int, view: View?) {
                view?.let {
                    val bottom = rv_items.bottom - AppRes.MAIN_BOTTOM_BAR_HEIGHT
                    processItemViewControlVisibility(position, view, AppRes.LMM_TOP_TAB_BAR_HIDE_AREA_HEIGHT, bottom)
                }
            }

            fun trackScrollOffset(rv: RecyclerView) {
                rv.linearLayoutManager()?.let {
                    val from = it.findFirstVisibleItemPosition()
                    val to = it.findLastVisibleItemPosition()
                    for (i in from..to) processItemView(i, it.findViewByPosition(i))
                }
            }

            super.onScrolled(rv, dx, dy)
            trackScrollOffset(rv)
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
