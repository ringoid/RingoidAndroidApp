package com.ringoid.origin.feed.view

import com.ringoid.base.view.Residual

open class ProfileResidual(val profileId: String) : Residual()
class DISCARD_PROFILE(profileId: String) : ProfileResidual(profileId)
object NO_IMAGES_IN_PROFILE : Residual()
