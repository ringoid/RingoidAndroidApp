package com.ringoid.origin.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.base.view.BaseActivity
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.ringoid.origin.navigation.splash
import com.ringoid.utility.getScreenWidth

class SplashActivity : BaseActivity<SplashViewModel>() {

    override fun getVmClass(): Class<SplashViewModel> = SplashViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onBeforeCreate() {
        setTheme(R.style.SplashTheme)

        if (BuildConfig.DEBUG) {
            spm.testBackup()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRes.SCREEN_WIDTH = getScreenWidth()

        vm.analyzeIntent(intent)
        vm.accessToken.observe(this, Observer {
            splash(this, path = it.getContentIfNotHandled()?.let { "/main" } ?: run { "/login" }, payload = intent)
        })
        vm.getAccessToken()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (isViewModelInitialized) {
            vm.analyzeIntent(intent)
        }
    }
}
