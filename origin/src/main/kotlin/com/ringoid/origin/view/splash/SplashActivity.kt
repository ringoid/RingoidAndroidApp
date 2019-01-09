package com.ringoid.origin.view.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.navigation.navigateAndClose

class SplashActivity : SimpleBaseActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.accessToken.observe(this, Observer {
            navigateAndClose(this, path = "/login")//it?.let { "/main" } ?: run { "/login" })
        })
        vm.obtainAccessToken()
    }
}
