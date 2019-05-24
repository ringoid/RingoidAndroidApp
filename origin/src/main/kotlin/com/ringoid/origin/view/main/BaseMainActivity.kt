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
import com.ringoid.base.view.BaseFragment
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.domain.model.push.PushNotificationData
import com.ringoid.origin.R
import com.ringoid.origin.navigation.NavigateFrom
import com.ringoid.origin.navigation.Payload
import com.ringoid.origin.view.base.BasePermissionActivity
import com.ringoid.origin.view.particles.ParticleAnimator
import com.ringoid.utility.changeVisibility
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
                initialize(index = FragNavController.TAB1, savedInstanceState = savedInstanceState)
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
            setOnNavigationItemReselectedListener { (fragNav.currentFrag as? BaseFragment<*>)?.onTabReselect() }
        }

        processExtras(intent, savedInstanceState)
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
        processExtras(intent, savedInstanceState)
        if (isStopped) {
            vm.onAppReOpen()
        }
    }

    override fun onStart() {
        super.onStart()
        debug_view.changeVisibility(isVisible = spm.isDebugLogEnabled())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        fragNav.onSaveInstanceState(outState)
        outState.putSerializable(BUNDLE_KEY_CURRENT_TAB, bottom_bar.selectedItem)
    }

    private fun processExtras(intent: Intent, savedInstanceState: Bundle?) {
        fun openExploreTab() {
            tabPayload = Payload.PAYLOAD_FEED_NEED_REFRESH
            openTabByName(tabName = NavigateFrom.MAIN_TAB_EXPLORE)
        }

        fun openLmmTab(tabName: String? = null) {
            tabPayload = tabName
            openTabByName(tabName = NavigateFrom.MAIN_TAB_LMM)
        }

        fun openProfileTab() {
            openTabByName(tabName = NavigateFrom.MAIN_TAB_PROFILE)
        }

        fun openInitialTab() {
            savedInstanceState?.getSerializable(BUNDLE_KEY_CURRENT_TAB)
                ?.let {
                    when (it) {
                        NavTab.EXPLORE -> openExploreTab()
                        NavTab.LMM -> openLmmTab()
                        NavTab.PROFILE -> openProfileTab()
                    }
                }
                ?: run { openExploreTab() }
        }

        intent.extras?.apply {
            Timber.v("Process extras[$savedInstanceState]: $this")
            getString("tab")?.let { tabName ->
                Timber.v("In-App extras: $tabName")
                tabPayload = getString("tabPayload")
                openTabByName(tabName)
            }
            ?: getString("data")?.let { data ->
                Timber.v("Push extras: $data")
                try {
                    val type = PushNotificationData.fromJson(data).type
                    when (type) {
                        PushNotificationData.TYPE_LIKE -> DomainUtil.SOURCE_FEED_LIKES
                        PushNotificationData.TYPE_MATCH -> DomainUtil.SOURCE_FEED_MATCHES
                        PushNotificationData.TYPE_MESSAGE -> DomainUtil.SOURCE_FEED_MESSAGES
                        else -> null
                    }
                    ?.let { openLmmTab(it) }
                    ?: run { openInitialTab() }
                } catch (e: Throwable) {  // JsonSyntaxException
                    Timber.e("Push extras not a JSON")
                    openInitialTab()
                }
            }
            ?: run { openInitialTab() }
        } ?: run { openInitialTab() }
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
        bottom_bar.selectedItem = tabNameToIndex(tabName)
    }

    // --------------------------------------------------------------------------------------------
    private fun tabNameToIndex(tabName: String): NavTab =
        when (tabName) {
            NavigateFrom.MAIN_TAB_EXPLORE -> NavTab.EXPLORE
            NavigateFrom.MAIN_TAB_LMM -> NavTab.LMM
            NavigateFrom.MAIN_TAB_PROFILE -> NavTab.PROFILE
            else -> throw IllegalArgumentException("Unknown tab name: $tabName")
        }

    // --------------------------------------------------------------------------------------------
    override fun showBadgeOnLmm(isVisible: Boolean) {
        bottom_bar.showBadgeOnLmm(isVisible)
    }

    override fun showBadgeWarningOnProfile(isVisible: Boolean) {
        bottom_bar.showWarningOnProfile(isVisible)
    }

    override fun showParticleAnimation(id: String, count: Int) {
//        particleAnimator.animate(id, count)
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
