package com.ringoid.origin.view.error

import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.origin.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.activity_no_network_connection.*

@AppNav("no_net_conn")
class NoNetworkConnectionActivity : BaseActivity<NoNetworkConnectionViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_no_network_connection

    override fun getVmClass() = NoNetworkConnectionViewModel::class.java

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLOSE -> finish()
            is ViewState.IDLE -> pb_no_connection.changeVisibility(isVisible = false)
            is ViewState.LOADING -> pb_no_connection.changeVisibility(isVisible = true)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_retry.clicks().compose(clickDebounce()).subscribe { vm.onPageReload() }
    }
}
