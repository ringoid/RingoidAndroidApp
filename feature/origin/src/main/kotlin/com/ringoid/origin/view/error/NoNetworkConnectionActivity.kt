package com.ringoid.origin.view.error

import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.origin.R
import com.ringoid.origin.view.base.theme.ThemedBaseActivity
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.connectivityManager
import com.ringoid.utility.targetVersion
import kotlinx.android.synthetic.main.activity_no_network_connection.*

@AppNav("no_net_conn")
class NoNetworkConnectionActivity : ThemedBaseActivity<NoNetworkConnectionViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_no_network_connection

    override fun getVmClass() = NoNetworkConnectionViewModel::class.java

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> {
                btn_retry.changeVisibility(isVisible = true)
                pb_no_connection.changeVisibility(isVisible = false)
            }
            is ViewState.LOADING -> {
                btn_retry.changeVisibility(isVisible = false)
                pb_no_connection.changeVisibility(isVisible = true)
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_retry.clicks().compose(clickDebounce()).subscribe { vm.onPageReload() }

        observeOneShot(vm.connectionRestoreOneShot()) { finish() }

        if (targetVersion(Build.VERSION_CODES.N)) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    runOnUiThread { vm.onPageReload() }
                }
            }
            application.applicationContext.connectivityManager()?.registerDefaultNetworkCallback(networkCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (targetVersion(Build.VERSION_CODES.N)) {
            application.applicationContext.connectivityManager()?.unregisterNetworkCallback(networkCallback)
            networkCallback = null
        }
    }
}
