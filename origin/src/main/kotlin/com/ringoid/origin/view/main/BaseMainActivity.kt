package com.ringoid.origin.view.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavLogger
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.origin.R
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

abstract class BaseMainActivity<VM : BaseMainViewModel> : BaseActivity<VM>(), IBaseMainActivity,
    FragNavController.TransactionListener {

    private lateinit var fragNav: FragNavController
    private val loginInMemoryCache: ILoginInMemoryCache by lazy { app.loginInMemoryCache }

    private var tabPayload: String? = null  // payload to pass to subscreen on tab switch

    // ------------------------------------------
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
                        bottom_bar.selectedItem = index
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
                (fragNav.currentFrag as? BaseFragment<*>)?.onBeforeTabSelect()
                fragNav.switchTab(it)
            }
            setOnNavigationItemReselectedListener { (fragNav.currentFrag as? BaseFragment<*>)?.onTabReselect() }
        }

        initializeFirebase()
        processExtras(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processExtras(intent)
    }

    override fun onStart() {
        super.onStart()
        debug_view.changeVisibility(isVisible = spm.isDebugLogEnabled())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
    }

    // --------------------------------------------------------------------------------------------
    private fun initializeFirebase() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener {
                it.takeIf { it.isSuccessful }
                    ?.result?.token
                    ?.let {
                        DebugLogUtil.i("Obtained Firebase push token: $it")
                        vm.updatePushToken(it)
                    }
            }
    }

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
        bottom_bar.selectedItem = tabNameToIndex(tabName)
    }

    // --------------------------------------------------------------------------------------------
    private fun tabNameToIndex(tabName: String): Int =
        when (tabName) {
            NavigateFrom.MAIN_TAB_LMM -> 0
            NavigateFrom.MAIN_TAB_PROFILE -> 1
            NavigateFrom.MAIN_TAB_FEED -> 2
            else -> throw IllegalArgumentException("Unknown tab name: $tabName")
        }

    // --------------------------------------------------------------------------------------------
    protected fun showBadgeOnLmm(isVisible: Boolean) {
        bottom_bar.showBadgeOnLmm(isVisible)
    }

    override fun showBadgeWarningOnProfile(isVisible: Boolean) {
        bottom_bar.showWarningOnProfile(isVisible)
    }

    // --------------------------------------------------------------------------------------------
    override fun isNewUser(): Boolean = loginInMemoryCache.isNewUser()
}
