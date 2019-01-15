package com.ringoid.main.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.feed.explore.ExploreFragment
import com.ringoid.origin.feed.lmm.LmmFragment
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.getString("tab")?.let { openTabByName(it) }
    }
}
