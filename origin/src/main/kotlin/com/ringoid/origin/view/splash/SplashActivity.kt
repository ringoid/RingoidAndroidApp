package com.ringoid.origin.view.splash

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.firebase.iid.FirebaseInstanceId
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.origin.navigation.splash
import io.branch.referral.Branch
import io.branch.referral.BranchError
import org.json.JSONObject
import timber.log.Timber

class SplashActivity : SimpleBaseActivity() {

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

        initializeBranch()
        initializeFirebase()

        vm.accessToken.observe(this, Observer {
            splash(this, path = it.getContentIfNotHandled()?.let { "/main" } ?: run { "/login" })
        })
        vm.obtainAccessToken()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    // --------------------------------------------------------------------------------------------
    private fun initializeBranch() {
        Branch.getInstance().initSession({ params: JSONObject, error: BranchError? ->
            error?.let { Timber.e("Branch error [${it.errorCode}]: ${it.message}") }
                 ?: run { Timber.i("Branch success: $params") }
        }, intent.data, this)
    }

    private fun initializeFirebase() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener {
                DebugLogUtil.i("FCM token: ${it.takeIf { it.isSuccessful }?.result?.token}")
            }
    }
}
