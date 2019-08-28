package com.ringoid.origin.profile.view

import com.ringoid.base.view.Residual

sealed class ReferralCode {

    object ReferralCodeAccepted : ReferralCode()
    object ReferralCodeDeclined : ReferralCode()
}

object REQUEST_TO_ADD_IMAGE : Residual()
