package com.ringoid.origin.feed.view.lmm

import com.ringoid.base.view.Residual
import com.ringoid.origin.feed.view.ProfileResidual
import com.ringoid.origin.view.main.LmmNavTab

data class RESTORE_CACHED_LIKES(val likedFeedItemIds: Map<String, MutableList<String>>) : Residual()
data class RESTORE_CACHED_USER_MESSAGES(val messagedFeedItemIds: Collection<String>) : Residual()

// visually display clear screen in mode DEFAULT and refresh spinner on all Lmm tabs except the specified one
data class CLEAR_AND_REFRESH_EXCEPT(val exceptLmmTab: LmmNavTab?) : Residual()

data class SEEN_ALL_FEED(val sourceFeed: Int) : Residual() {
    companion object {
        const val FEED_LIKES = 0
        const val FEED_MATCHES = 1
        const val FEED_MESSENGER = 2
    }
}

class TRANSFER_PROFILE(profileId: String) : ProfileResidual(profileId)
