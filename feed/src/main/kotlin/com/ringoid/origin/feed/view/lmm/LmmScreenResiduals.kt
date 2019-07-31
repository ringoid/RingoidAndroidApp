package com.ringoid.origin.feed.view.lmm

import com.ringoid.base.view.Residual
import com.ringoid.origin.feed.view.ProfileResidual
import com.ringoid.origin.view.main.LmmNavTab

// visually display clear screen in mode DEFAULT and refresh spinner on all Lmm tabs except the specified one
@Deprecated("LMM -> LC")
data class CLEAR_AND_REFRESH_EXCEPT(val exceptLmmTab: LmmNavTab?) : Residual()

data class SEEN_ALL_FEED(val sourceFeed: Int) : Residual() {
    companion object {
        const val FEED_LIKES = 0
        const val FEED_MATCHES = 1
        const val FEED_MESSENGER = 2
    }
}

class TRANSFER_PROFILE(profileId: String) : ProfileResidual(profileId)
