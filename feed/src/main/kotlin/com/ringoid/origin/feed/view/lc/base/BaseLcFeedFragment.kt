package com.ringoid.origin.feed.view.lc.base

import android.os.Bundle
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.utility.runOnUiThread
import kotlinx.android.synthetic.main.fragment_feed.*

abstract class BaseLcFeedFragment<VM : BaseLcFeedViewModel> : FeedFragment<VM>() {

    protected abstract fun getSourceFeed(): LcNavTab

    override fun getAddPhotoDialogDescriptionResId(): Int = OriginR_string.feed_lmm_dialog_no_user_photo_description

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
}
