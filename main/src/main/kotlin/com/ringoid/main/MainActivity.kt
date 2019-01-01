package com.ringoid.main

import android.os.Bundle
import com.ncapdevi.fragnav.FragNavController
import com.ringoid.origin.R
import com.ringoid.origin.profile.view.ProfileFragment
import com.ringoid.origin.view.feed.ExploreFragment
import com.ringoid.origin.view.feed.LmmFragment
import com.ringoid.origin.view.main.BaseMainActivity

class MainActivity : BaseMainActivity<MainViewModel>() {

    private lateinit var fragNav: FragNavController

    override fun getVmClass() = MainViewModel::class.java

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragNav = FragNavController(supportFragmentManager, R.id.fl_container)
            .apply { rootFragments = listOf(ExploreFragment.newInstance(), LmmFragment.newInstance(), ProfileFragment.newInstance()) }
    }
}
