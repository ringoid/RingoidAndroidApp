package com.ringoid.origin.feed.view.lmm

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ringoid.origin.feed.view.lmm.like.LikesFeedFragment
import com.ringoid.origin.feed.view.lmm.match.MatchesFeedFragment
import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import java.lang.ref.WeakReference

class LmmPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val map = SparseArray<WeakReference<Fragment>>()

    fun accessItem(position: Int): Fragment? = map[position]?.get()

    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> MessengerFragment.newInstance()
            1 -> MatchesFeedFragment.newInstance()
            2 -> LikesFeedFragment.newInstance()
            else -> throw IllegalArgumentException("Page at position [$position] is not allowed")
        }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        map.put(position, WeakReference(fragment))
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        map.remove(position)
        super.destroyItem(container, position, `object`)
    }
}
