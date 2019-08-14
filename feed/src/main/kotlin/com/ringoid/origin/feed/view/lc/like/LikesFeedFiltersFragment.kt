package com.ringoid.origin.feed.view.lc.like

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.view.lc.base.ILcFeedFiltersHost
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersFragment
import com.ringoid.origin.view.filters.LC_COUNTS
import com.ringoid.utility.communicator

class LikesFeedFiltersFragment : LcFeedFiltersFragment<LikesFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFiltersFragment = LikesFeedFiltersFragment()
    }

    override fun getVmClass(): Class<LikesFeedFiltersViewModel> = LikesFeedFiltersViewModel::class.java

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is LC_COUNTS ->
                        (newState.residual as LC_COUNTS).let { counts ->
                            communicator(ILcFeedFiltersHost::class.java)?.let {
                                it.setCountOfFilteredFeedItems(counts.countLikes)
                                it.setTotalNotFilteredFeedItems(counts.totalNotFilteredLikes)
                            }
                        }
                }
            }
        }
    }
}
