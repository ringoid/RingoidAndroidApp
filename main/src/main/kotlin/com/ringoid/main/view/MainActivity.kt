package com.ringoid.main.view

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
import com.ringoid.main.OriginR_style
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import com.ringoid.origin.profile.view.UserProfileFragment
import com.ringoid.origin.view.main.BaseMainActivity

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    @StyleRes private var currentThemeResId: Int = 0

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                ExploreFragment.newInstance(),
                LmmFragment.newInstance(),
                MessengerFragment.newInstance(),
                UserProfileFragment.newInstance())

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentThemeResId = spm.getThemeResId(defaultThemeResId = OriginR_style.AppTheme)
        observe(vm.badgeLmm, ::showBadgeOnLmm)
        observe(vm.badgeMessenger, ::showBadgeOnMessenger)
    }

    override fun onStart() {
        super.onStart()
        if (currentThemeResId != spm.getThemeResId(defaultThemeResId = currentThemeResId)) {
            recreate()  // theme has changed outside, in some another Activity
        }
    }
}
