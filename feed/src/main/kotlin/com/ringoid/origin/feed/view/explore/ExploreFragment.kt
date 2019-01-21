package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import kotlinx.android.synthetic.main.fragment_feed.*

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_explore_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_explore_empty_need_refresh)
            else -> null
        }

    // --------------------------------------------------------------------------------------------
    override fun onTabReselect() {
        super.onTabReselect()
        (rv_items?.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(0, 0)
    }

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        if (isActivityCreated) {
            /**
             * Purge feed when Main tab has switched back to Explore screen,
             * swipe-to-refresh is required to get need data.
             */
            vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed) { feedAdapter.submitList(it.profiles) }
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.onRefresh() }
        }
    }
}
