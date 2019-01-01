package com.ringoid.origin.view.main

import android.os.Bundle
import com.ncapdevi.fragnav.FragNavController
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R
import com.ringoid.origin.view.feed.ExploreFragment
import com.ringoid.origin.view.feed.LmmFragment

class MainActivity : BaseActivity<MainViewModel>() {

    private lateinit var fragNav: FragNavController

    override fun getVmClass() = MainViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_main

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragNav = FragNavController(supportFragmentManager, R.id.fl_container)
            .apply { rootFragments = listOf(ExploreFragment.newInstance(), LmmFragment.newInstance(), ProfileFragment.newInstance()) }
    }
}
