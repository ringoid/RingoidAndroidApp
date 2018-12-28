package com.ringoid.origin.auth.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.auth.R
import com.ringoid.origin.auth.WidgetR_drawable
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.inputDebounce
import com.ringoid.widget.WidgetState
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<LoginViewModel>() {

    override fun getVmClass() = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_login

    companion object {
        fun getCallingIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> {
                btn_login.changeVisibility(isVisible = true, soft = true)
                pb_login.changeVisibility(isVisible = false)
            }
            is ViewState.LOADING -> {
                btn_login.changeVisibility(isVisible = false, soft = true)
                pb_login.changeVisibility(isVisible = true)
            }
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
            }
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
        tv_sex_male.clicks().compose(clickDebounce()).subscribe {
            tv_sex_male.takeIf { it.isSelected } ?: run {
                tv_sex_male.isSelected = true
                tv_sex_female.isSelected = false
                vm.onGenderSelect(Gender.MALE)
            }
        }
        tv_sex_female.clicks().compose(clickDebounce()).subscribe {
            tv_sex_female.takeIf { it.isSelected } ?: run {
                tv_sex_male.isSelected = false
                tv_sex_female.isSelected = true
                vm.onGenderSelect(Gender.FEMALE)
            }
        }

        vm.loginButtonEnableState.observe(this, Observer { btn_login.isEnabled = it })
        vm.yearOfBirthEntryState.observe(this, Observer {
            when (it) {
                WidgetState.NORMAL -> {
                    et_year_of_birth.setBackgroundResource(WidgetR_drawable.rect_round_grey)
                    iv_status.also { it.changeVisibility(isVisible = false) }
                }
                WidgetState.ACTIVE -> {
                    et_year_of_birth.setBackgroundResource(WidgetR_drawable.rect_round_green)
                    iv_status.also {
                        it.changeVisibility(isVisible = true)
                        it.setImageResource(R.drawable.ic_check_green_16dp)
                    }
                }
                WidgetState.ERROR -> {
                    et_year_of_birth.setBackgroundResource(WidgetR_drawable.rect_round_orange)
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
