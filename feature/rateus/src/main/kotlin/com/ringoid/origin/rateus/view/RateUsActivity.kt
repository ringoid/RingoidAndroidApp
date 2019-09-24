package com.ringoid.origin.rateus.view

import androidx.fragment.app.DialogFragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.dialog.BaseDialogHostActivity

@AppNav("rate_us")
class RateUsActivity : BaseDialogHostActivity() {

    override fun getFragmentTag(): String = RateUsDialog.TAG
    override fun instantiateFragment(): DialogFragment = RateUsDialog.newInstance()
}
