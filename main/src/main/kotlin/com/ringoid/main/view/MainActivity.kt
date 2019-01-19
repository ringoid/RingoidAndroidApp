package com.ringoid.main.view

import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.messenger.view.MessengerFragment
import com.ringoid.origin.profile.view.profile.ProfileFragment
import com.ringoid.origin.view.main.BaseMainActivity

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                ExploreFragment.newInstance(),
                LmmFragment.newInstance(),
                MessengerFragment.newInstance(),
                ProfileFragment.newInstance())

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
}
