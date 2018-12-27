package com.ringoid.origin.view.auth

import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.origin.R
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.inputDebounce
import com.ringoid.widget.WidgetState
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
        et_year_of_birth.textChanges().compose(inputDebounce()).subscribe { vm.onYearOfBirthChange(it.toString()) }

        vm.loginButtonEnableState.observe(this, Observer { btn_login.isEnabled = it })
        vm.yearOfBirthEntryState.observe(this, Observer {
            when (it) {
                WidgetState.NORMAL -> {
                    et_year_of_birth.setBackgroundResource(R.drawable.rect_round_grey)
                    iv_status.also {
                        it.changeVisibility(isVisible = false)
                    }
                }
                WidgetState.ACTIVE -> {
                    et_year_of_birth.setBackgroundResource(R.drawable.rect_round_green)
                    iv_status.also {
                        it.changeVisibility(isVisible = true)
                        it.setImageResource(R.drawable.ic_check_green_16dp)
                    }
                }
                WidgetState.ERROR -> {
                    et_year_of_birth.setBackgroundResource(R.drawable.rect_round_orange)
                    iv_status.also {
                        it.changeVisibility(isVisible = true)
                        it.setImageResource(R.drawable.ic_error_red_16dp)
                    }
                }
                else -> { /* no-op */ }
            }
        })
    }
}
