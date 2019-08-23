package com.ringoid.origin.feed.view.lc.base

import com.ringoid.utility.ICommunicator

interface ILcFeedFiltersHost : ICommunicator {

    fun setCountOfFilteredFeedItems(count: Int)
    fun setTotalNotFilteredFeedItems(count: Int)
}
