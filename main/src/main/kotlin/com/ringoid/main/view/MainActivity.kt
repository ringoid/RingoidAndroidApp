package com.ringoid.main.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
import com.ringoid.main.R
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import com.ringoid.origin.profile.view.profile.ProfileFragment
import com.ringoid.origin.view.main.BaseMainActivity
import com.ringoid.utility.changeVisibility

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    private var badge: View? = null

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                ExploreFragment.newInstance(),
                LmmFragment.newInstance(),
                MessengerFragment.newInstance(),
                ProfileFragment.newInstance())

    // --------------------------------------------------------------------------------------------
    private fun showBadgeOnLmm(isVisible: Boolean) {
        badge?.changeVisibility(isVisible, soft = true)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observe(vm.badgeLmm, ::showBadgeOnLmm)

        // TODO: not working
        val menuView = bottomBar().getChildAt(0) as? BottomNavigationMenuView
        menuView
            ?.getChildAt(0)
            ?.let { it as? BottomNavigationItemView }
            ?.let {
                badge = LayoutInflater.from(this@MainActivity).inflate(R.layout.main_menu_badge, menuView, false)
                it.addView(badge)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        badge = null
    }
}
