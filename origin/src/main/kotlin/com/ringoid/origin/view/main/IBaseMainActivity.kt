package com.ringoid.origin.view.main

import com.ringoid.utility.ICommunicator

interface IBaseMainActivity : ICommunicator {

    fun isNewUser(): Boolean

    fun showBadgeOnLmm(isVisible: Boolean)
    fun showBadgeWarningOnProfile(isVisible: Boolean)
}
