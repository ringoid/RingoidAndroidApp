package com.ringoid.origin.view.error

import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.SimpleBaseActivity
import com.ringoid.origin.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.activity_no_network_connection.*

@AppNav("no_net_conn")
class NoNetworkConnectionActivity : SimpleBaseActivity() {

    override fun getLayoutId(): Int = R.layout.activity_no_network_connection

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: wait for connection restore and retry last request
        btn_retry.clicks().compose(clickDebounce()).subscribe {  }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // TODO: retry on back pressed and show this page again if error
    }
}
