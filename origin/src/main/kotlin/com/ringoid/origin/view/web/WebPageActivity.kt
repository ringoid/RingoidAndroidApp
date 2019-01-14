package com.ringoid.origin.view.web

import android.os.Bundle
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.R
import com.ringoid.origin.view.base.BaseHostActivity

@AppNav("webpage")
class WebPageActivity : BaseHostActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = WebPageFragment.newInstance(webUrl = intent.extras?.getString("url"))
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, WebPageFragment.TAG)
                .commitNow()
        }
    }
}
