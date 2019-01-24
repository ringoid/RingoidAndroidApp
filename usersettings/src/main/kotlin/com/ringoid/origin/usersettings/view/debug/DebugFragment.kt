package com.ringoid.origin.usersettings.view.debug

import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseFragment
import com.ringoid.usersettings.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_debug.*

class DebugFragment : BaseFragment<DebugViewModel>() {

    companion object {
        const val TAG = "DebugFragment_tag"

        fun newInstance(): DebugFragment = DebugFragment()
    }

    override fun getVmClass(): Class<DebugViewModel> = DebugViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_debug

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item_error_http.clicks().compose(clickDebounce()).subscribe {  }
        item_error_token.clicks().compose(clickDebounce()).subscribe {  }
        item_error_app_version.clicks().compose(clickDebounce()).subscribe {  }
        item_error_server.clicks().compose(clickDebounce()).subscribe {  }
        item_error_timeout.clicks().compose(clickDebounce()).subscribe {  }
    }
}
