package com.ringoid.origin.feed.view.lc

import com.ringoid.base.view.Residual
import com.ringoid.origin.view.main.LmmNavTab

// visually display clear screen in mode DEFAULT and refresh spinner on all Lmm tabs except the specified one
@Deprecated("LMM -> LC")
data class CLEAR_AND_REFRESH_EXCEPT(val exceptLmmTab: LmmNavTab?) : Residual()

data class LC_FEED_COUNTS(val show: Int, val hidden: Int) : Residual()

data class SEEN_ALL_FEED(val sourceFeed: Int) : Residual() {
    companion object {
        const val FEED_LIKES = 0
        const val FEED_MATCHES = 1
        const val FEED_MESSENGER = 2
    }
}

class TRANSFER_PROFILE(val profileId: String) : Residual()
