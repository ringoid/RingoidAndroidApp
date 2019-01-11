package com.ringoid.origin.view.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.airbnb.deeplinkdispatch.DeepLink
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavLogger
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.view.BaseActivity
import com.ringoid.origin.R
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>() {

    protected lateinit var fragNav: FragNavController

    override fun getLayoutId(): Int = R.layout.activity_main

    protected abstract fun getListOfRootFragments(): List<Fragment>

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragNav = FragNavController(supportFragmentManager, R.id.fl_container)
            .apply {
                rootFragments = getListOfRootFragments()
                navigationStrategy = UnlimitedTabHistoryStrategy(object : FragNavSwitchController {
                    override fun switchTab(index: Int, transactionOptions: FragNavTransactionOptions?) {
                        bottom_bar.selectedItemId = indexToTabId(index)
                    }
                })
                fragNavLogger = object : FragNavLogger {
                    override fun error(message: String, throwable: Throwable) {
                        Timber.e(throwable, message)
                    }
                }
                fragmentHideStrategy = FragNavController.DETACH_ON_NAVIGATE_HIDE_ON_SWITCH
                createEager = true
                initialize(index = FragNavController.TAB1, savedInstanceState = savedInstanceState)
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
    }

    // --------------------------------------------------------------------------------------------
    private fun indexToTabId(index: Int): Int =
        when (index) {
            1 -> R.id.item_lmm
            2 -> R.id.item_profile
            else -> R.id.item_feed
        }
}
