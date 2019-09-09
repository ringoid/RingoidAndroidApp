package com.ringoid.origin.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.ringoid.origin.navigation.splash
import com.ringoid.origin.view.base.theme.ThemedBaseActivity
import com.ringoid.utility.getScreenWidth
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject
import timber.log.Timber

class SplashActivity : ThemedBaseActivity<SplashViewModel>() {

    override fun getVmClass(): Class<SplashViewModel> = SplashViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onBeforeCreate() {
        setTheme(R.style.SplashTheme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppRes.SCREEN_WIDTH = getScreenWidth()

        initializeBranch()

        vm.analyzeIntent(intent)
        vm.accessToken().observe(this, Observer {
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

    // --------------------------------------------------------------------------------------------
    private fun initializeBranch() {
        Branch.getInstance().initSession({ params: JSONObject, error: BranchError? ->
            error?.let { Timber.e("Branch error [${it.errorCode}]: ${it.message}") }
                 ?: run { Timber.i("Branch success: $params") }
        }, intent.data, this)
    }
}
