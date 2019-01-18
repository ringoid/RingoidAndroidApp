package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import android.view.View
import com.ringoid.origin.feed.view.FeedFragment
import kotlinx.android.synthetic.main.fragment_feed.*

class ExploreFragment : FeedFragment<ExploreViewModel>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.getFeed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipe_refresh_layout.apply {
//            setColorSchemeResources(*resources.getIntArray(R.array.swipe_refresh_colors))
            setOnRefreshListener { vm.onRefresh() }
        }
    }
}
