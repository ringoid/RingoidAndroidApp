package com.ringoid.origin.feed.view.lc.messenger

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.view.lc.base.ILcFeedFiltersHost
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersFragment
import com.ringoid.origin.view.filters.LC_COUNTS
import com.ringoid.utility.communicator

class MessagesFeedFiltersFragment : LcFeedFiltersFragment<MessagesFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFiltersFragment = MessagesFeedFiltersFragment()
    }

    override fun getVmClass(): Class<MessagesFeedFiltersViewModel> = MessagesFeedFiltersViewModel::class.java

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is LC_COUNTS ->
                        (newState.residual as LC_COUNTS).let { counts ->
                            communicator(ILcFeedFiltersHost::class.java)?.let {
                                it.setCountOfFilteredFeedItems(counts.countMessages)
                                it.setTotalNotFilteredFeedItems(counts.totalNotFilteredMessages)
                            }
                        }
                }
            }
        }
    }
}