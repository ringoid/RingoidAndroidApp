package com.ringoid.origin.view.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.domain.BuildConfig

class SplashActivity : SimpleBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = Uri.parse("${BuildConfig.AUTHORITY}/login")
        startActivity(Intent(Intent.ACTION_VIEW, url))
        finish()
    }
}
