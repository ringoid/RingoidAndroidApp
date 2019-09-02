package com.ringoid.origin.view.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import androidx.fragment.app.Fragment
import com.ncapdevi.fragnav.FragNavController
import com.ncapdevi.fragnav.FragNavLogger
import com.ncapdevi.fragnav.FragNavSwitchController
import com.ncapdevi.fragnav.FragNavTransactionOptions
import com.ncapdevi.fragnav.tabhistory.UnlimitedTabHistoryStrategy
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.origin.R
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.view.base.BasePermissionActivity
import com.ringoid.origin.view.particles.ParticleAnimator
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.checkForNull
import com.ringoid.utility.collection.toJsonObject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

abstract class BaseMainActivity<VM : BaseMainViewModel> : BasePermissionActivity<VM>(), IBaseMainActivity,
    FragNavController.TransactionListener {

    companion object {
        private const val BUNDLE_KEY_CURRENT_TAB = "bundle_key_current_tab"
    }

    @Inject lateinit var particleAnimator: ParticleAnimator

    private lateinit var fragNav: FragNavController
    private val powerSafeModeReceiver = PowerSafeModeBroadcastReceiver()
    private val loginInMemoryCache: ILoginInMemoryCache by lazy { app.loginInMemoryCache }

    private var tabPayload: String? = null  // payload to pass to subscreen on tab switch

    // ------------------------------------------
    override fun getLayoutId(): Int = R.layout.activity_main

    protected abstract fun getListOfRootFragments(): List<Fragment>

    override fun appScreen(): AppScreen = AppScreen.MAIN

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragNav = FragNavController(supportFragmentManager, R.id.fl_container)
            .apply {
                rootFragments = getListOfRootFragments()
                navigationStrategy = UnlimitedTabHistoryStrategy(object : FragNavSwitchController {
                    override fun switchTab(index: Int, transactionOptions: FragNavTransactionOptions?) {
                        bottom_bar.selectedItem = NavTab.get(index)
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
                initialize(index = FragNavController.TAB1, savedInstanceState = null)
            }

        registerReceiver(powerSafeModeReceiver, IntentFilter().apply { addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) })

        bottom_bar.apply {
            setOnNavigationItemSelectedListener {
                (fragNav.currentFrag as? BaseFragment<*>)?.let {
                    it.onBeforeTabSelect()
                    it.setLastTabTransactionPayload(tabPayload)
                }
                fragNav.switchTab(it.ordinal)
            }
            setOnNavigationItemReselectedListener {
                (fragNav.currentFrag as? BaseFragment<*>)?.onTabReselect(tabPayload)
            }
        }

        Timber.i("Cold start")
        processExtras(intent, savedInstanceState)
        DebugLogUtil.clear()  // clear debug logs when recreate Main screen
    }

    /**
     * App has been started already, but it's been requested to restart somehow. This may be caused
     * either by click on app's launcher icon on device's Home screen, or by click on push notification.
     * In these cases normally app starts from Splash screen and then navigates here to Main screen,
     * but the latter had been started already and sits on some single task (launch mode is 'singleTask'),
     * so this Main screen just receives 'new Intent' and restarts without recreation.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.i("Warm start")
        processExtras(intent, savedInstanceState)  // app's warm start
    }

    override fun onStart() {
        super.onStart()
        showDebugView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
        outState.putSerializable(BUNDLE_KEY_CURRENT_TAB, bottom_bar.selectedItem)
    }

    private fun processExtras(intent: Intent, savedInstanceState: Bundle?) {
        fun openExploreTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_EXPLORE)
        }

        fun openLikesTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_LIKES)
        }

        fun openMessagesTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_MESSAGES)
        }

        fun openLcTab(lcTab: LcNavTab? = null) {
            lcTab?.let {
                tabPayload = it.feedName
                val tabName = when (it) {
                    LcNavTab.LIKES -> NavigateFrom.MAIN_TAB_LIKES
                    LcNavTab.MESSAGES -> NavigateFrom.MAIN_TAB_MESSAGES
                }
                openTabByName(tabName = tabName)
            }
        }

        fun openProfileTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_PROFILE)
        }

        fun openMainTab(tab: NavTab? = null) {
            if (tab == null) {  // open default tab
                openProfileTab()
                return
            }

            when (tab) {
                NavTab.EXPLORE -> openExploreTab()
                NavTab.LIKES -> openLikesTab()
                NavTab.MESSAGES -> openMessagesTab()
                NavTab.PROFILE -> openProfileTab()
            }
        }

        fun openInitialTab() {
            savedInstanceState?.getSerializable(BUNDLE_KEY_CURRENT_TAB)
                ?.let { it as? NavTab }
                ?.let { openMainTab(tab = it) }
                ?: run { openMainTab(tab = bottom_bar.selectedItem) }
        }

        Timber.d("Intent data: ${intent.extras?.toJsonObject()}")
        intent.extras?.apply {
            Timber.v("Process extras[${checkForNull(savedInstanceState)}]: $this")
            // in-app navigation
            getString("tab")?.let { tabName ->
                Timber.v("In-App extras: $tabName")
                tabPayload = getString("tabPayload")
                Timber.i("Open $tabName for in-app navigation")
                openTabByName(tabName)
            }
            // app is opened from notification
            ?: getString("type")?.let { type ->
                Timber.v("Push extras: $type")
                vm.onPushOpen()
                LcNavTab.fromPushType(pushType = type)
                    ?.let {
                        Timber.i("Open ${it.feedName} tab (by notification)")
                        openLcTab(lcTab = it)
                    }
                    ?: run {
                        Timber.i("Open init tab (unknown notification type)")
                        openInitialTab()  // unknown notification type
                    }
            }
            ?: run {
                // neither in-app navigation nor open from notification, possibly clicking on launcher icon
                Timber.i("Open init tab (splash)")
                openInitialTab()
            }
        } ?: run {
            // app's cold start
            Timber.i("Open init tab (no extra)")
            openInitialTab()
        }
    }

    override fun onStop() {
        super.onStop()
        particleAnimator.terminate()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerSafeModeReceiver)
    }

    // ------------------------------------------
    override fun onFragmentTransaction(fragment: Fragment?, transactionType: FragNavController.TransactionType) {
    }

    override fun onTabTransaction(fragment: Fragment?, index: Int) {
        Timber.v("Switched tab[$index]: ${fragment?.javaClass?.simpleName}, payload: $tabPayload")
        (fragment as? BaseFragment<*>)?.onTabTransaction(payload = tabPayload)
        tabPayload = null  // consume tab payload on the opened tab
    }

    private fun openTabByName(tabName: String) {
        bottom_bar.selectedItem = NavTab.from(tabName)
    }

    // --------------------------------------------------------------------------------------------
    override fun showBadgeOnLikes(isVisible: Boolean) {
        bottom_bar.showBadgeOnLikes(isVisible)
    }

    override fun showBadgeOnMessages(isVisible: Boolean) {
        bottom_bar.showBadgeOnMessages(isVisible)
    }

    override fun showBadgeWarningOnProfile(isVisible: Boolean) {
        bottom_bar.showWarningOnProfile(isVisible)
    }

    override fun showParticleAnimation(id: String, count: Int) {
        particleAnimator.animate(id, count)
    }

    @DebugOnly
    protected fun showDebugView() {
        debug_view.changeVisibility(isVisible = spm.isDebugLogEnabled())
    }

    // --------------------------------------------------------------------------------------------
    override fun isNewUser(): Boolean = loginInMemoryCache.isNewUser()

    // --------------------------------------------------------------------------------------------
    inner class PowerSafeModeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            particleAnimator.terminate()
        }
    }
}
