package com.ringoid.origin.feed.view.lc.messenger

import android.os.Bundle
import com.ringoid.base.observeOneShot
import com.ringoid.origin.feed.view.lc.base.ILcFeedFiltersHost
import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersFragment
import com.ringoid.utility.communicator

class MessagesFeedFiltersFragment : LcFeedFiltersFragment<MessagesFeedFiltersViewModel>() {

    companion object {
        fun newInstance(): MessagesFeedFiltersFragment = MessagesFeedFiltersFragment()
    }

    override fun getVmClass(): Class<MessagesFeedFiltersViewModel> = MessagesFeedFiltersViewModel::class.java

    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeOneShot(vm.filterCountsOneShot()) { counts ->
            communicator(ILcFeedFiltersHost::class.java)?.let {
                it.setCountOfFilteredFeedItems(counts.countMessages)
                it.setTotalNotFilteredFeedItems(counts.totalNotFilteredMessages)
            }
        }
    }
}