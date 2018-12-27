package com.ringoid.origin.view.auth

import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.origin.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<LoginViewModel>() {

    override fun getVmClass() = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_login

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            ViewState.IDLE -> {
                btn_login.changeVisibility(isVisible = true)
                pb_login.changeVisibility(isVisible = false)
            }
            ViewState.LOADING -> {
                btn_login.changeVisibility(isVisible = false)
                pb_login.changeVisibility(isVisible = true)
            }
            ViewState.ERROR -> {}
            else -> { /* no-op */ }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btn_login.clicks().compose(clickDebounce()).subscribe { vm.login() }
    }
}
