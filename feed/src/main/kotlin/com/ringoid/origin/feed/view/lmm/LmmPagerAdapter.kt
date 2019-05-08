package com.ringoid.origin.feed.view.lmm

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.feed.view.lmm.like.LikesFeedFragment
import com.ringoid.origin.feed.view.lmm.match.MatchesFeedFragment
import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import java.lang.ref.WeakReference

class LmmPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val map = SparseArray<WeakReference<Fragment>>()

    fun accessItem(position: Int): Fragment? = map[position]?.get()

    fun accessItemByName(feedName: String): Fragment? =
        getItemPositionByName(feedName)
            .takeIf { it != DomainUtil.BAD_POSITION }
            ?.let { map[it]?.get() }

    fun doForEachItem(action: (item: Fragment?) -> Unit) {
        for (i in 0 until count) {
            action.invoke(map[i]?.get())
        }
    }

    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment =
        when (position) {
            0 -> LikesFeedFragment.newInstance()
            1 -> MatchesFeedFragment.newInstance()
            2 -> MessengerFragment.newInstance()
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

    // ------------------------------------------
    private fun getItemPositionByName(feedName: String): Int =
        when (feedName) {
            DomainUtil.SOURCE_FEED_LIKES -> 0
            DomainUtil.SOURCE_FEED_MATCHES -> 1
            DomainUtil.SOURCE_FEED_MESSAGES -> 2
            else -> DomainUtil.BAD_POSITION
        }
}
