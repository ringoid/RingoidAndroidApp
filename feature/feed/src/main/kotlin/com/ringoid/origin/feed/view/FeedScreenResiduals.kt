package com.ringoid.origin.feed.view

import com.ringoid.base.view.Residual

open class ProfileResidual(val profileId: String) : Residual()

/**
 * User discards profile manually (via transition (LIKE) or block (BLOCK / REPORT),
 * so need to handle VIEW aobjs for profiles that comes into viewport after removal
 * animation finishes.
 */
class DISCARD_PROFILE(profileId: String) : ProfileResidual(profileId)

object REFRESH : Residual()  // refresh programmatically
