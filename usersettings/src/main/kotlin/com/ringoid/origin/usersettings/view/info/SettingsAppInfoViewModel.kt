package com.ringoid.origin.usersettings.view.info

import android.app.Application
import androidx.fragment.app.Fragment
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.origin.AppRes
import com.ringoid.origin.navigation.navigate
import javax.inject.Inject

class SettingsAppInfoViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    object InternalNavigator {
        fun openDebugScreen(fragment: Fragment) {
            navigate(fragment, path="/debug")
        }

        fun openLicensesWebPage(fragment: Fragment) {
            navigate(fragment, path = "/webpage?url=${AppRes.WEB_URL_LICENSES}")
        }

        fun openPrivacyWebPage(fragment: Fragment) {
            navigate(fragment, path = "/webpage?url=${AppRes.WEB_URL_PRIVACY}")
        }

        fun openTermsWebPage(fragment: Fragment) {
            navigate(fragment, path = "/webpage?url=${AppRes.WEB_URL_TERMS}")
        }
    }

    // --------------------------------------------------------------------------------------------
    fun onDebugClick() {
        navigation.value = InternalNavigator::openDebugScreen
    }

    fun onLicensesClick() {
        navigation.value = InternalNavigator::openLicensesWebPage
    }

    fun onPrivacyClick() {
        navigation.value = InternalNavigator::openPrivacyWebPage
    }

    fun onTermsClick() {
        navigation.value = InternalNavigator::openTermsWebPage
    }
}
