package com.ringoid.origin.view.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavLogger
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.R
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.getAttributeDrawable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>(), IBaseMainActivity,
    FragNavController.TransactionListener {

    protected lateinit var fragNav: FragNavController

    override val imagesViewPool = RecyclerView.RecycledViewPool()
    private lateinit var badgeLmm: View

    private var tabPayload: String? = null  // payload to pass to subscreen on tab switch

    override fun getLayoutId(): Int = R.layout.activity_main

    protected abstract fun getListOfRootFragments(): List<Fragment>

    protected fun bottomBar() = bottom_bar

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()

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
            setOnNavigationItemSelectedListener {
                changeMenuItemAppearance(it)
                fragNav.switchTab(tabIdToIndex(it.itemId))
                    true
            }
            setOnNavigationItemReselectedListener { (fragNav.currentFrag as? BaseFragment<*>)?.onTabReselect() }
        }

        processExtras(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processExtras(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
    }

    // --------------------------------------------------------------------------------------------
    private fun processExtras(intent: Intent) {
        fun openInitialTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_PROFILE)
        }

        intent.extras?.apply {
            getString("tab")
                ?.let {
                    tabPayload = getString("tabPayload")
                    openTabByName(it)
                } ?: run { openInitialTab() }
        } ?: run { openInitialTab() }
    }

    // ------------------------------------------
    override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
    }

    override fun onTabTransaction(fragment: Fragment?, index: Int) {
        Timber.v("Switched tab[$index]: ${fragment?.javaClass?.simpleName}, payload: $tabPayload")
        (fragment as? BaseFragment<*>)?.onTabTransaction(payload = tabPayload)
        tabPayload = null  // consume tab payload on the opened tab
    }

    protected fun openTabByName(tabName: String) {
        bottom_bar.selectedItemId = tabNameToId(tabName)
    }

    // --------------------------------------------------------------------------------------------
    private fun changeMenuItemAppearance(item: MenuItem) {
        if (bottom_bar.selectedItemId == item.itemId) {
            return  // no change
        }

        val prevItem = bottom_bar.menu.findItem(bottom_bar.selectedItemId)
        prevItem.icon = getAttributeDrawable(tabIdToAttr(prevItem.itemId).first)
        item.icon = getAttributeDrawable(tabIdToAttr(item.itemId).second)
    }

    // ------------------------------------------
    private fun indexToTabId(index: Int): Int =
        when (index) {
            0 -> R.id.item_lmm
            1 -> R.id.item_profile
            2 -> R.id.item_feed
            else -> throw IllegalArgumentException("Index of tab exceeds bounds: $index")
        }

    private fun tabIdToIndex(tabId: Int): Int =
        when (tabId) {
            R.id.item_lmm -> 0
            R.id.item_profile -> 1
            R.id.item_feed -> 2
            else -> throw IllegalArgumentException("Unknown tab id: $tabId")
        }

    private fun tabNameToId(tabName: String): Int =
        when (tabName) {
            NavigateFrom.MAIN_TAB_LMM -> R.id.item_lmm
            NavigateFrom.MAIN_TAB_PROFILE -> R.id.item_profile
            NavigateFrom.MAIN_TAB_FEED -> R.id.item_feed
            else -> throw IllegalArgumentException("Unknown tab name: $tabName")
        }

    private fun tabIdToAttr(tabId: Int): Pair<Int, Int> =
        when (tabId) {
            R.id.item_lmm -> R.attr.refDrawableBottomBarLmm to R.attr.refDrawableBottomBarLmmPressed
            R.id.item_profile -> R.attr.refDrawableBottomBarProfile to R.attr.refDrawableBottomBarProfilePressed
            R.id.item_feed -> R.attr.refDrawableBottomBarExplore to R.attr.refDrawableBottomBarExplore
            else -> throw IllegalArgumentException("Unknown tab id: $tabId")
        }

    // --------------------------------------------------------------------------------------------
    private fun initView() {
        val menuView = bottom_bar.getChildAt(0) as? BottomNavigationMenuView
        badgeLmm = LayoutInflater.from(this).inflate(R.layout.main_menu_badge, menuView, false)

        menuView?.getChildAt(tabIdToIndex(R.id.item_lmm))
                ?.let { it as? BottomNavigationItemView }
                ?.addView(badgeLmm)

        showBadgeOnLmm(isVisible = false)
    }

    protected fun showBadgeOnLmm(isVisible: Boolean) {
        badgeLmm.changeVisibility(isVisible, soft = true)
    }
}
