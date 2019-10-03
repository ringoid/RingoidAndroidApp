package com.ringoid.origin.feed.view.lc.base

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.ViewState
import com.ringoid.debug.DebugLogUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.lc.FeedCounts
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import com.ringoid.utility.runOnUiThread
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class BaseLcFeedFragment<VM : BaseLcFeedViewModel> : FeedFragment<VM>(), ILcFeedFiltersHost {

    protected var lcCountHidden: Int = 0
    protected var lcCountShow: Int = 0

    protected abstract fun getSourceFeed(): LcNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    override fun onClearState(mode: Int) {
        super.onClearState(mode)
        if (mode != ViewState.CLEAR.MODE_CHANGE_FILTERS) {
            setDefaultToolbarTitle()
        }
    }

    override fun onDiscardAllProfiles() {
        if (lcCountHidden > 0) {
            onClearState(ViewState.CLEAR.MODE_CHANGE_FILTERS)  // discard all profiles in Feed, but there are hidden in LC
        } else {
            super.onDiscardAllProfiles()
        }
    }

    override fun onDiscardProfile(profileId: String): FeedItemVO? =
        super.onDiscardProfile(profileId)?.also { _ ->
            requestFiltersForUpdateOnChangeLcFeed()
            setToolbarTitleWithLcCounts(--lcCountShow, lcCountHidden)
        }

    override fun onNoImagesInUserProfile(redirectBackOnFeedScreen: Boolean) {
        super.onNoImagesInUserProfile(redirectBackOnFeedScreen)
        if (!redirectBackOnFeedScreen) {
            onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // purge LC feed when user has no images in profile
        }
    }

    override fun onRefreshGesture() {
        vm.dropFilters()  // manual refresh acts as 'show all', but selected filters remain, though not applied
        super.onRefreshGesture()
    }

    /**
     * Some feed items haven't been seen by user on a particular LC feed, specified by [sourceFeed].
     */
    private fun onNotSeenAllFeed(sourceFeed: LcNavTab) {
        DebugLogUtil.v("There are not seen items [$sourceFeed]")
        when (sourceFeed) {
            LcNavTab.LIKES -> communicator(IBaseMainActivity::class.java)?.showBadgeOnLikes(true)
            LcNavTab.MESSAGES -> communicator(IBaseMainActivity::class.java)?.showBadgeOnMessages(true)
        }
    }

    /**
     * All feed items on a particular LC feed, specified by [sourceFeed],
     * have been seen by user, so it's time to hide red badge on a corresponding LC tab.
     */
    private fun onSeenAllFeed(sourceFeed: LcNavTab) {
        DebugLogUtil.v("All seen [$sourceFeed]")
        when (sourceFeed) {
            LcNavTab.LIKES -> communicator(IBaseMainActivity::class.java)?.showBadgeOnLikes(false)
            LcNavTab.MESSAGES -> communicator(IBaseMainActivity::class.java)?.showBadgeOnMessages(false)
        }
    }

    private fun updateFeedCounts(feedCounts: FeedCounts) {
        setCountOfFilteredFeedItems(count = feedCounts.show)
        setTotalNotFilteredFeedItems(count = feedCounts.show + feedCounts.hidden)
        setToolbarTitleWithLcCounts(show = feedCounts.show, hidden = feedCounts.hidden)
    }

    // ------------------------------------------
    protected abstract fun setDefaultToolbarTitle()

    protected open fun setToolbarTitleWithLcCounts(show: Int, hidden: Int) {
        lcCountShow = show
        lcCountHidden = hidden
    }

    protected fun requestFiltersForUpdateOnChangeLcFeed() {
        childFragmentManager.findFragmentByTag(BaseFiltersFragment.TAG)
            ?.let { it as? BaseFiltersFragment<*> }
            ?.requestFiltersForUpdate()
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.feed()) {
                if (it.isNotEmpty()) {
                    toolbarWidget?.restoreScrollFlags()
                }
                feedAdapter.submitList(it)
                runOnUiThread { rv_items?.let { scrollListToPosition(0) } }
            }
            observe(vm.refreshOnPush(), ::showRefreshPopup)
            observeOneShot(vm.feedCountsOneShot(), ::updateFeedCounts)
            observeOneShot(vm.notSeenAllFeedItemsOneShot(), ::onNotSeenAllFeed)
            observeOneShot(vm.seenAllFeedItemsOneShot(), ::onSeenAllFeed)
            observeOneShot(vm.lmmLoadFailedOneShot()) {
                it.handleOnView(this@BaseLcFeedFragment, {}) { vm.refresh() /** refresh on connection timeout */ }
            }
        }
    }

    @Suppress("AutoDispose", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClearState(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // LC feed is initially purged

        // refresh by click on 'tap to refresh' popup
        btn_refresh_popup.clicks().compose(clickDebounce()).subscribe {
            filtersPopupWidget?.hide()
            vm.onTapToRefreshClick()
        }

        filtersPopupWidget?.let { widget ->
            widget.setCountOfFilteredFeedItems(String.format(AppRes.FILTER_BUTTON_APPLY, 0))
            widget.setOnClickListener_applyFilters {
                it.hide()
                vm.onApplyFilters()
            }
            widget.setOnClickListener_showAll {
                it.hide()
                vm.onShowAllWithoutFilters()
            }
        }
    }
}
