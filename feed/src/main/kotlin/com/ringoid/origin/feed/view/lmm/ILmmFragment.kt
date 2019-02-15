package com.ringoid.origin.feed.view.lmm

import com.ringoid.utility.ICommunicator

interface ILmmFragment : ICommunicator {

    fun accessViewModel(): LmmViewModel
    fun showTabs(isVisible: Boolean)
}
