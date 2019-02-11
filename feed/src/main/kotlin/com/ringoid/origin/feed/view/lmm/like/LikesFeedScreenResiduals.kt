package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.base.view.Residual

data class HAS_LIKES_ON_PROFILE(val feedItemPosition: Int) : Residual()
data class NO_LIKES_ON_PROFILE(val feedItemPosition: Int) : Residual()
