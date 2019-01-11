package com.ringoid.main.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.base.deeplink.AppNav
import com.ringoid.origin.profile.view.profile.ProfileFragment
import com.ringoid.origin.view.feed.explore.ExploreFragment
import com.ringoid.origin.view.feed.lmm.LmmFragment
import com.ringoid.origin.view.main.BaseMainActivity
import kotlinx.android.synthetic.main.activity_main.*

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                ExploreFragment.newInstance(),
                LmmFragment.newInstance(),
                ProfileFragment.newInstance())

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.getString("tab")?.let { bottom_bar.selectedItemId = tabNameToId(it) }
    }
}
