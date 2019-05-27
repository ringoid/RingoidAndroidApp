package com.ringoid.origin.feed.view.lmm

import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.utility.ICommunicator

interface ILmmFragment : ICommunicator {

    fun accessViewModel(): LmmViewModel

    fun showBadgeOnLikes(isVisible: Boolean)
    fun showBadgeOnMatches(isVisible: Boolean)
    fun showBadgeOnMessenger(isVisible: Boolean)
    fun showTabs(isVisible: Boolean)

    fun transferProfile(profileId: String, destinationFeed: LmmNavTab)
    fun transferProfile(discarded: FeedItemVO?, destinationFeed: LmmNavTab)
}
