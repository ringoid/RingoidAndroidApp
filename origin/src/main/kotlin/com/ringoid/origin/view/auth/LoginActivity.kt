package com.ringoid.origin.view.auth

import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.origin.R

class LoginActivity : BaseActivity<LoginViewModel>() {

    override fun getVmClass() = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_login

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            ViewState.LOADING -> {}
            ViewState.ERROR -> {}
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
