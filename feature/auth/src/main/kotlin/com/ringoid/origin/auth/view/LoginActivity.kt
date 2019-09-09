package com.ringoid.origin.auth.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.observe
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.ViewState
import com.ringoid.domain.Onboarding
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.auth.OriginR_string
import com.ringoid.origin.auth.R
import com.ringoid.origin.auth.WidgetR_drawable
import com.ringoid.origin.auth.memory.LoginInMemoryCache
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.navigation.*
import com.ringoid.origin.style.ThemeUtils
import com.ringoid.origin.view.base.theme.ThemedBaseActivity
import com.ringoid.utility.AutoLinkMovementMethod
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.inputDebounce
import com.ringoid.widget.WidgetState
import kotlinx.android.synthetic.main.activity_login.*

@AppNav("login")
class LoginActivity : ThemedBaseActivity<LoginViewModel>() {

    companion object {
        private const val BUNDLE_KEY_SELECTED_GENDER = "bundle_key_selected_gender"
    }

    private val imagePreviewReceiver: IImagePreviewReceiver by lazy { app.imagePreviewReceiver }
    private val loginInMemoryCache: LoginInMemoryCache by lazy { app.loginInMemoryCache as LoginInMemoryCache }

    // ------------------------------------------
    override fun getVmClass() = LoginViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_login

    override fun appScreen(): AppScreen = AppScreen.LOGIN

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
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
            else -> { /* no-op */ }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.hasExtra(Extras.EXTRA_LOGOUT)) {
            imagePreviewReceiver.clear()  // forget any previous image cropping result upon logout
            vm.onLogout()
        }

        with(btn_login) {
            when (Onboarding.current()) {
                Onboarding.ADD_IMAGE -> OriginR_string.login_button
                Onboarding.DIRECT -> OriginR_string.login_button_direct
            }.let { setText(it) }
            clicks().compose(clickDebounce()).subscribe { vm.login() }
        }
        et_year_of_birth.apply {
            requestFocus()
            setOnKeyPreImeListener { keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                    finish()
                }
                false
            }
            textChanges().compose(inputDebounce()).subscribe { vm.onYearOfBirthChange(it.toString()) }
        }
        switch_theme.apply {
            setOnCheckedChangeListener(null)
            isChecked = !ThemeUtils.isDefaultTheme(styleSpm)
            setOnCheckedChangeListener { _, _ -> vm.switchTheme() }
        }
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
        tv_terms.movementMethod = object : AutoLinkMovementMethod() {
            override fun processUrl(url: String) {
                navigate(this@LoginActivity, path = "/webpage?url=$url")
            }
        }

        observe(vm.loginButtonEnableState()) { btn_login.isEnabled = it }
        observe(vm.yearOfBirthEntryState()) {
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
                        it.setImageResource(WidgetR_drawable.ic_error_red_16dp)
                    }
                }
                else -> { /* no-op */ }
            }
        }
        observeOneShot(vm.changeThemeOneShot()) { recreate() }
        observeOneShot(vm.loginUserOneShot()) {
            loginInMemoryCache.setNewUser(true)
            when (Onboarding.current()) {
                Onboarding.ADD_IMAGE -> ExternalNavigator.openGalleryToGetImage(this)
                Onboarding.DIRECT -> navigateAndClose(this, path = "/main?tab=${NavigateFrom.MAIN_TAB_EXPLORE}")
            }
        }

        savedInstanceState?.let {
            (it.getSerializable(BUNDLE_KEY_SELECTED_GENDER) as? Gender)
                ?.let { gender -> vm.onGenderSelect(gender) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ExternalNavigator.RC_GALLERY_GET_IMAGE -> {
                when (resultCode) {
                    Activity.RESULT_CANCELED -> navigateAndClose(this, path = "/main?tab=${NavigateFrom.MAIN_TAB_PROFILE}")
                    Activity.RESULT_OK -> {
                        data?.putExtra(Extras.EXTRA_NAVIGATE_FROM, NavigateFrom.SCREEN_LOGIN)
                        navigateAndClose(this, path = "/imagepreview", payload = data)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(BUNDLE_KEY_SELECTED_GENDER, vm.gender)
    }
}
