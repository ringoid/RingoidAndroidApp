package com.ringoid.origin.view.main

import com.ringoid.utility.ICommunicator

interface IBaseMainActivity : ICommunicator {

    fun isNewUser(): Boolean

    fun decrementCountOnLikes(decrementBy: Int = 1)
    fun decrementCountOnMessages(decrementBy: Int = 1)

    fun showBadgeOnLikes(isVisible: Boolean)
    fun showBadgeOnMessages(isVisible: Boolean)
    fun showBadgeWarningOnProfile(isVisible: Boolean)

    fun showCountOnLikes(count: Int)
    fun showCountOnMessages(count: Int)

    fun showParticleAnimation(id: String, count: Int = 1)
}
