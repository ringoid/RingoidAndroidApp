package com.ringoid.main

import androidx.fragment.app.Fragment
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lc.like.LikesFeedFragment
import com.ringoid.origin.feed.view.lc.messenger.MessagesFeedFragment
import com.ringoid.origin.profile.view.UserProfileFragment
import com.ringoid.origin.view.main.NavTab

fun listOfMainScreens(): List<Fragment> =
    mutableListOf<Fragment>().apply {
        NavTab.values.forEach {
            val fragment = when (it) {
                NavTab.EXPLORE -> ExploreFragment.newInstance()
                NavTab.LIKES -> LikesFeedFragment.newInstance()
                NavTab.MESSAGES -> MessagesFeedFragment.newInstance()
                NavTab.PROFILE -> UserProfileFragment.newInstance()
            } as Fragment
            add(fragment)
        }
    }
