package com.ringoid.origin.view.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.R
import com.ringoid.origin.navigation.splash

class SplashActivity : SimpleBaseActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onBeforeCreate() {
        setTheme(R.style.SplashTheme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.accessToken.observe(this, Observer {
            splash(this, path = it?.let { "/main" } ?: run { "/login" })
        })
        vm.obtainAccessToken()
    }
}
