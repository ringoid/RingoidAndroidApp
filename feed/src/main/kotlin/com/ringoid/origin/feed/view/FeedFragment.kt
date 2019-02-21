package com.ringoid.origin.feed.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseListFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.IProfile
import com.ringoid.domain.model.image.EmptyImage
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideControls
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.navigation.*
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.*
import com.ringoid.utility.collection.EqualRange
import com.ringoid.widget.view.swipes
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_feed.*
import timber.log.Timber

abstract class FeedFragment<VM : FeedViewModel, T : IProfile> : BaseListFragment<VM>() {

    protected lateinit var feedAdapter: BaseFeedAdapter<T, OriginFeedViewHolder<T>>
        private set
    private lateinit var feedTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>
    private lateinit var imagesTrackingBus: TrackingBus<EqualRange<ProfileImageVO>>

    override fun getLayoutId(): Int = R.layout.fragment_feed
    override fun getRecyclerView(): RecyclerView = rv_items

    protected abstract fun createFeedAdapter(): BaseFeedAdapter<T, OriginFeedViewHolder<T>>

    protected abstract fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input?

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            fl_empty_container.changeVisibility(isVisible = false, soft = true)
            swipe_refresh_layout.isRefreshing = false
        }
        fun onClearState(mode: Int) {
            feedAdapter.clear()
            getEmptyStateInput(mode)?.let {
                onIdleState()
                fl_empty_container.changeVisibility(isVisible = true)
                val emptyFragment = EmptyFragment.newInstance(it)
                childFragmentManager
                    .beginTransaction()
                    .replace(R.id.fl_empty_container, emptyFragment, EmptyFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
        }
        fun onErrorState() {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> onClearState(mode = newState.mode)
            is ViewState.DONE -> {
                when (newState.residual) {
                    is BLOCK_PROFILE -> {
                        feedAdapter.remove { it.id == (newState.residual as BLOCK_PROFILE).profileId }
                    }
                    is NO_IMAGES_IN_PROFILE -> {
                        Dialogs.showTextDialog(activity,
                            descriptionResId = OriginR_string.feed_explore_dialog_no_user_photo_description,
                            positiveBtnLabelResId = OriginR_string.button_add_photo,
                            negativeBtnLabelResId = OriginR_string.button_later,
                            positiveListener = { _, _ -> navigate(this@FeedFragment, path="/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}&tabPayload=${Payload.PAYLOAD_PROFILE_REQUEST_ADD_IMAGE}") })
                        swipe_refresh_layout.isRefreshing = false
                    }
                }
            }
            is ViewState.IDLE -> onIdleState()
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onErrorState)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isActivityCreated && hidden) {
            /**
             * Purge feed when Main tab has switched from this Feed screen,
             * swipe-to-refresh is required to get need data.
             */
            vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedAdapter = createFeedAdapter().apply {
            onInsertItemsListener = { trackVisibility() }
            settingsClickListener = { model: T, position: Int, positionOfImage: Int ->
                val image = model.images[positionOfImage]
                scrollToTopOfItemAtPositionAndPost(position).post {
                    notifyItemChanged(position, FeedViewHolderHideControls)
                }
                communicator(ILmmFragment::class.java)?.showTabs(isVisible = false)
                navigate(this@FeedFragment, path = "/block_dialog?position=$position&profileId=${model.id}&imageId=${image.id}&excludedReasons=10,50,70", rc = RequestCode.RC_BLOCK_DIALOG)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RequestCode.RC_BLOCK_DIALOG -> {
                if (data == null) {
                    // TODO: crashes on app recreate, save bundle lack of 'data'
                    val e = NullPointerException("No output from Block/Report dialog - this is an error!")
                    Timber.e(e); throw e
                }

                val position = data.extras!!.getString("position", "0").toInt()
                communicator(ILmmFragment::class.java)?.apply {
                    showTabs(isVisible = true)
                    if (position == 1) {
                        scrollToTopOfItemAtPosition(0)
                    }
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
                } else {
                    // on dialog dismiss = show controls back
                    getRecyclerView().post {
                        feedAdapter.notifyItemChanged(position, FeedViewHolderShowControls)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        feedTrackingBus = TrackingBus(onSuccess = Consumer(vm::onView), onError = Consumer(Timber::e))
        imagesTrackingBus = TrackingBus(onSuccess = Consumer(vm::onView), onError = Consumer(Timber::e))
        feedAdapter.trackingBus = imagesTrackingBus
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_items.apply {
            adapter = feedAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
//            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)
            addOnScrollListener(itemOffsetScrollListener)
            addOnScrollListener(visibilityTrackingScrollListener)
        }
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            refreshes().compose(clickDebounce()).subscribe { vm.onRefresh() }
            swipes().compose(clickDebounce()).subscribe { vm.onStartRefresh() }
        }
        scroll_fab.clicks().compose(clickDebounce()).subscribe {
            scroll_fab.changeVisibility(isVisible = false)
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
            removeOnScrollListener(visibilityTrackingScrollListener)
        }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    private val itemOffsetScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                // TODO
            }
        }
    }

    // ------------------------------------------
    protected val topScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            rv.linearLayoutManager()?.let {
                if (dy > 0) {  // scroll list down - to see new items
                    if (scroll_fab.isVisible()) {
                        scroll_fab.changeVisibility(isVisible = false)
                    }
                } else {  // scroll list up - to see previous items
                    val offset = rv.computeVerticalScrollOffset()
                    if (scroll_fab.isVisible()) {
                        if (offset <= 0) {
                            scroll_fab.changeVisibility(isVisible = false)
                        }
                    } else {
                        if (offset > 0) {
                            scroll_fab.changeVisibility(isVisible = true)
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------
    private val visibilityTrackingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            trackVisibility(rv)
        }
    }

    private fun trackVisibility(rv: RecyclerView) {
        rv.linearLayoutManager()?.let {
            val from = it.findFirstVisibleItemPosition()
            val to = it.findLastVisibleItemPosition()
            Timber.d("Feed Adapter: $feedAdapter, range [$from, $to]")  // TODO: remove log in release
            val items = feedAdapter.getItemsExposed(from = from, to = to)
            // TODO: find a way to 'getCurrentImagePosition' and set it instead of '0' properly
            var range = EqualRange(from = from, to = to,
                items = items.map {
                    val image = if (it.isRealModel) it.images[0] else EmptyImage
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

    private fun trackVisibility() = rv_items.post { trackVisibility(rv_items) }
}
