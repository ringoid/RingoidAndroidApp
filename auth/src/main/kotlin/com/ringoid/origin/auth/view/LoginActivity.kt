package com.ringoid.origin.auth.view

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.ViewState
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.auth.R
import com.ringoid.origin.auth.WidgetR_drawable
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.navigation.Extras
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.navigateAndClose
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.inputDebounce
import com.ringoid.widget.WidgetState
import kotlinx.android.synthetic.main.activity_login.*

@AppNav("login")
class LoginActivity : BaseActivity<LoginViewModel>() {

    override fun getVmClass() = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_login

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            btn_login.changeVisibility(isVisible = true, soft = true)
            pb_login.changeVisibility(isVisible = false)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> {
                btn_login.changeVisibility(isVisible = false, soft = true)
                pb_login.changeVisibility(isVisible = true)
            }
            is ViewState.ERROR -> {
                // TODO: analyze: newState.e
                Dialogs.showTextDialog(this, titleResId = R.string.error_common, description = "DL TEXT FROM URL")
                onIdleState()
            }
            else -> { /* no-op */ }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
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
        vm.navigation.observe(this, Observer { it.call(this) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalNavigator.RC_GALLERY_GET_IMAGE -> {
                data?.putExtra(Extras.EXTRA_NAVIGATE_FROM, NavigateFrom.SCREEN_LOGIN)
                navigateAndClose(this, path = "/imagepreview", payload = data)
            }
        }
    }
}
