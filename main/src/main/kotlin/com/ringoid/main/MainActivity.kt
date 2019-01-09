package com.ringoid.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ringoid.origin.profile.view.profile.ProfileFragment
import com.ringoid.origin.view.feed.explore.ExploreFragment
import com.ringoid.origin.view.feed.lmm.LmmFragment
import com.ringoid.origin.view.main.BaseMainActivity

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
    }
}
