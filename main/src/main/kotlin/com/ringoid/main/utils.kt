package com.ringoid.main

import androidx.fragment.app.Fragment
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.profile.view.UserProfileFragment
import com.ringoid.origin.view.main.NavTab

fun listOfMainScreens(): List<Fragment> =
    mutableListOf<Fragment>().apply {
        NavTab.values.forEach {
            val fragment = when (it) {
                NavTab.EXPLORE -> ExploreFragment.newInstance()
                NavTab.LMM -> LmmFragment.newInstance()
                NavTab.PROFILE -> UserProfileFragment.newInstance()
            }
            add(fragment)
        }
    }
