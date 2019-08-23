package com.ringoid.origin.view.web

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("webpage")
class WebPageActivity : BaseHostActivity() {

    override fun getFragmentTag(): String = WebPageFragment.TAG
    override fun instantiateFragment(): Fragment = WebPageFragment.newInstance(webUrl = intent.extras?.getString("url"))
}
