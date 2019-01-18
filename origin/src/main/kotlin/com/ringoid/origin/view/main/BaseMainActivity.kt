package com.ringoid.origin.view.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavLogger
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R
import com.ringoid.origin.navigation.NavigateFrom
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>(),
    FragNavController.TransactionListener {

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
                transactionListener = this@BaseMainActivity
                initialize(index = FragNavController.TAB1, savedInstanceState = savedInstanceState)
            }

        bottom_bar.apply {
            setOnNavigationItemSelectedListener { fragNav.switchTab(tabIdToIndex(it.itemId)) ; true }
            setOnNavigationItemReselectedListener { (fragNav.currentFrag as? BaseFragment<*>)?.onTabReselect() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
    }

    // --------------------------------------------------------------------------------------------
    override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
    }

    override fun onTabTransaction(fragment: Fragment?, index: Int) {
        Timber.v("Switched tab[$index]: ${fragment?.javaClass?.simpleName}")
        (fragment as? BaseFragment<*>)?.onTabTransaction()
    }

    protected fun openTabByName(tabName: String) {
        bottom_bar.selectedItemId = tabNameToId(tabName)
    }

    // --------------------------------------------------------------------------------------------
    private fun indexToTabId(index: Int): Int =
        when (index) {
            0 -> R.id.item_feed
            1 -> R.id.item_lmm
            2 -> R.id.item_messages
            3 -> R.id.item_profile
            else -> R.id.item_feed
        }

    private fun tabIdToIndex(tabId: Int): Int =
        when (tabId) {
            R.id.item_feed -> 0
            R.id.item_lmm -> 1
            R.id.item_messages -> 2
            R.id.item_profile -> 3
            else -> 0
        }

    private fun tabNameToId(tabName: String): Int =
        when (tabName) {
            NavigateFrom.MAIN_TAB_FEED -> R.id.item_feed
            NavigateFrom.MAIN_TAB_LMM -> R.id.item_lmm
            NavigateFrom.MAIN_TAB_MESSENGER -> R.id.item_messages
            NavigateFrom.MAIN_TAB_PROFILE -> R.id.item_profile
            else -> throw IllegalArgumentException("Unknown tab name: $tabName")
        }
}
