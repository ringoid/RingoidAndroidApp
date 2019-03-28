package com.ringoid.main.view

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
import com.ringoid.main.OriginR_style
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.profile.view.UserProfileFragment
import com.ringoid.origin.utils.AppUtils
import com.ringoid.origin.view.main.BaseMainActivity

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    private var currentLocale: String? = null
    @StyleRes private var currentThemeResId: Int = 0

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                LmmFragment.newInstance(),
                UserProfileFragment.newInstance(),
                ExploreFragment.newInstance())

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocale = app.localeManager.getLang()
        currentThemeResId = spm.getThemeResId(defaultThemeResId = OriginR_style.AppTheme_Dark)
        observe(vm.badgeLmm, ::showBadgeOnLmm)
        observe(vm.badgeWarningProfile, ::showBadgeWarningOnProfile)
        AppUtils.checkForGooglePlayServices(this)
    }

    override fun onStart() {
        super.onStart()
        if (currentLocale != app.localeManager.getLang() ||
            currentThemeResId != spm.getThemeResId(defaultThemeResId = currentThemeResId)) {
            recreate()  // locale or theme has changed outside, in some another Activity
        }
    }

    override fun onResume() {
        super.onResume()
        AppUtils.checkForGooglePlayServices(this)
    }
}
