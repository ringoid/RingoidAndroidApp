package com.ringoid.main.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import com.ringoid.origin.profile.view.profile.UserProfileFragment
import com.ringoid.origin.view.main.BaseMainActivity

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

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
        observe(vm.badgeLmm, ::showBadgeOnLmm)
        observe(vm.badgeMessenger, ::showBadgeOnMessenger)
    }
}
