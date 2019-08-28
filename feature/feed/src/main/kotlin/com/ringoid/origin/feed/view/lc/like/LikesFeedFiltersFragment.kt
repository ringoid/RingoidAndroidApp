package com.ringoid.origin.feed.view.lc.like

import android.os.Bundle
import com.ringoid.base.observeOneShot
import com.ringoid.origin.feed.view.lc.base.ILcFeedFiltersHost
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersFragment
import com.ringoid.utility.communicator

class LikesFeedFiltersFragment : LcFeedFiltersFragment<LikesFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFiltersFragment = LikesFeedFiltersFragment()
    }

    override fun getVmClass(): Class<LikesFeedFiltersViewModel> = LikesFeedFiltersViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeOneShot(vm.filterCountsOneShot()) { counts ->
            communicator(ILcFeedFiltersHost::class.java)?.let {
                it.setCountOfFilteredFeedItems(counts.countLikes)
                it.setTotalNotFilteredFeedItems(counts.totalNotFilteredLikes)
            }
        }
    }
}
