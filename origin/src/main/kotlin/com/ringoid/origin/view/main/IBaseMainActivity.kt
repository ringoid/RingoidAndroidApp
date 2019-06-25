package com.ringoid.origin.view.main

import com.ringoid.utility.ICommunicator

interface IBaseMainActivity : ICommunicator {

    fun isNewUser(): Boolean

    fun decrementCountOnLmm(decrementBy: Int = 1)

    fun showBadgeOnLmm(isVisible: Boolean)
    fun showBadgeWarningOnProfile(isVisible: Boolean)
    fun showCountOnLmm(count: Int)
    fun showParticleAnimation(id: String, count: Int = 1)
}
