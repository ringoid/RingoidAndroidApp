package com.ringoid.origin.view.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.domain.BuildConfig

class SplashActivity : SimpleBaseActivity() {

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.accessToken.observe(this, Observer {
            val path = it?.let { "/main" } ?: run { "/login" }
            val url = Uri.parse("${BuildConfig.APPNAV}$path")
            startActivity(Intent(Intent.ACTION_VIEW, url))
            finish()
        })
        vm.obtainAccessToken()
    }
}
