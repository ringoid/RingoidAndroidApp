package com.ringoid.origin.feed.view.lmm

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ringoid.origin.feed.view.lmm.like.LikesFeedFragment
import com.ringoid.origin.feed.view.lmm.match.MatchesFeedFragment

class LmmPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> LikesFeedFragment.newInstance()
            1 -> MatchesFeedFragment.newInstance()
            else -> throw IllegalArgumentException("Page at position [$position] is not allowed")
        }
}
