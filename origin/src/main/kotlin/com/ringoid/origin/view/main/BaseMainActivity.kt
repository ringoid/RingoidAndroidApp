package com.ringoid.origin.view.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R
import com.ringoid.origin.view.feed.explore.ExploreFragment
import com.ringoid.origin.view.feed.lmm.LmmFragment
import kotlinx.android.synthetic.main.activity_main.*

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>() {

    protected lateinit var fragNav: FragNavController

    override fun getLayoutId(): Int = R.layout.activity_main

    protected abstract fun getListOfRootFragment(): List<Fragment>

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragNav = FragNavController(supportFragmentManager, R.id.fl_container)
            .apply {
                rootFragments = getListOfRootFragment()
                navigationStrategy = UnlimitedTabHistoryStrategy(object : FragNavSwitchController {
                    override fun switchTab(index: Int, transactionOptions: FragNavTransactionOptions?) {
                        bottom_bar.selectedItemId
                    }
                })
                initialize(index = FragNavController.TAB1, savedInstanceState = savedInstanceState)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
    }
}
