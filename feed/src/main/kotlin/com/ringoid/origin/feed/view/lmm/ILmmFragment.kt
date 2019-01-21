package com.ringoid.origin.feed.view.lmm

import com.ringoid.utility.ICommunicator

interface ILmmFragment : ICommunicator {

    fun showBadgeOnLikes(isVisible: Boolean)
    fun showBadgeOnMatches(isVisible: Boolean)
}
