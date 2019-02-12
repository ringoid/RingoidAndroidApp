package com.ringoid.origin.view.main

import com.ringoid.utility.ICommunicator

interface IBaseMainActivity : ICommunicator {

    fun isNewUser(): Boolean
    fun onRefreshFeed()

    fun showBadgeWarningOnProfile(isVisible: Boolean)
}
