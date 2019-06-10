package com.ringoid.origin.feed.view

import com.ringoid.base.view.Residual

open class ProfileResidual(val profileId: String) : Residual()

/**
 * User discards profile manually (via transition (LIKE) or block (BLOCK / REPORT),
 * so need to handle VIEW aobjs for profiles that comes into viewport after removal
 * animation finishes.
 */
class DISCARD_PROFILE(profileId: String) : ProfileResidual(profileId)
data class DISCARD_PROFILES(val profileIds: Collection<String>) : Residual() {  // discard multiple profiles

    override fun toString(): String = "${super.toString()}(${profileIds.joinToString { it.substring(0..3) }})"
}

object NO_IMAGES_IN_PROFILE : Residual()
object REFRESH : Residual()  // refresh programmatically
