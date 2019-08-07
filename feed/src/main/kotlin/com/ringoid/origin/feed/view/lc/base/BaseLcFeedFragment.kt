package com.ringoid.origin.feed.view.lc.base

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.feed.view.NO_IMAGES_IN_PROFILE
import com.ringoid.origin.feed.view.lmm.SEEN_ALL_FEED
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import com.ringoid.utility.runOnUiThread
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class BaseLcFeedFragment<VM : BaseLcFeedViewModel> : FeedFragment<VM>() {

    override fun createFiltersFragment(): BaseFiltersFragment<*> = LcFeedFiltersFragment.newInstance()

    protected abstract fun getSourceFeed(): LcNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is NO_IMAGES_IN_PROFILE -> vm.onNoImagesInProfile()
                    /**
                     * All feed items on a particular Lmm feed, specified by [SEEN_ALL_FEED.sourceFeed],
                     * have been seen by user, so it's time to hide red badge on a corresponding Lmm tab.
                     */
                    is SEEN_ALL_FEED -> {
                        (newState.residual as SEEN_ALL_FEED)
                            .let {
                                when (it.sourceFeed) {
                                    SEEN_ALL_FEED.FEED_LIKES -> communicator(IBaseMainActivity::class.java)?.showBadgeOnLikes(false)
                                    SEEN_ALL_FEED.FEED_MESSENGER -> communicator(IBaseMainActivity::class.java)?.showBadgeOnMessages(false)
                                    else -> { /* no-op */ }
                                }
                            }
                        }
                    }
            }
        }
    }

    override fun onRefreshGesture() {
        Bus.post(event = BusEvent.RefreshOnLc(lcSourceFeed = getSourceFeed().feedName))
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.feed) {
                feedAdapter.submitList(it)
                runOnUiThread { rv_items?.let { scrollListToPosition(0) } }
            }
            observe(vm.refreshOnPush) { showRefreshPopup(isVisible = it) }
        }
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)  // LC feed is initially purged
    }

    @Suppress("AutoDispose", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // refresh by click on 'tap to refresh' popup
        btn_refresh_popup.clicks().compose(clickDebounce()).subscribe { vm.onTapToRefreshClick() }
    }
}
