package com.ringoid.origin.feed.view.lmm

import com.ringoid.base.view.Residual

data class RESTORE_CACHED_LIKES(val likedFeedItemIds: Map<String, MutableList<String>>) : Residual()
data class RESTORE_CACHED_USER_MESSAGES(val messagedFeedItemIds: Collection<String>) : Residual()

data class SEEN_ALL_FEED(val sourceFeed: Int) : Residual() {
    companion object {
        const val FEED_LIKES = 0
        const val FEED_MATCHES = 1
        const val FEED_MESSENGER = 2
    }
}

data class TRANSFER_PROFILE(val profileId: String) : Residual()
