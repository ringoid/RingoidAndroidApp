package com.ringoid.origin.feed.view.lmm

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.feed.R
import com.ringoid.utility.changeTypeface
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.fragment_lmm.*

class LmmFragment : BaseFragment<LmmViewModel>(), ILmmFragment {

    companion object {
        fun newInstance(): LmmFragment = LmmFragment()
    }

    private lateinit var lmmPagesAdapter: LmmPagerAdapter

    override fun getVmClass(): Class<LmmViewModel> = LmmViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_lmm

    // --------------------------------------------------------------------------------------------
    // TODO: save that fields onSaveInstanceState() for later restore
    private var badge_likes_visibilityPrev: Boolean = false
    private var badge_matches_visibilityPrev: Boolean = false
    private var badge_messages_visibilityPrev: Boolean = false

    override fun accessViewModel(): LmmViewModel = vm

    override fun showBadgeOnLikes(isVisible: Boolean) {
        if (vp_pages?.currentItem == 2) {
            return
        }
        badge_likes_visibilityPrev = isVisible
        btn_tab_likes.showBadge(isVisible)
    }

    override fun showBadgeOnMatches(isVisible: Boolean) {
        if (vp_pages?.currentItem == 1) {
            return
        }
        badge_matches_visibilityPrev = isVisible
        btn_tab_matches.showBadge(isVisible)
    }

    override fun showBadgeOnMessenger(isVisible: Boolean) {
        if (vp_pages?.currentItem == 0) {
            return
        }
        badge_messages_visibilityPrev = isVisible
        btn_tab_messenger.showBadge(isVisible)
    }

    override fun showTabs(isVisible: Boolean) {
        if (isVisible) {
            if (badge_likes_visibilityPrev) {
                btn_tab_likes.showBadge(isVisible)
            }
            if (badge_matches_visibilityPrev) {
                btn_tab_matches.showBadge(isVisible)
            }
            if (badge_messages_visibilityPrev) {
                btn_tab_messenger.showBadge(isVisible)
            }
        } else {
            btn_tab_likes.showBadge(isVisible)
            btn_tab_matches.showBadge(isVisible)
            btn_tab_messenger.showBadge(isVisible)
        }
        btn_tab_likes.changeVisibility(isVisible)
        btn_tab_matches.changeVisibility(isVisible)
        btn_tab_messenger.changeVisibility(isVisible)
        tab_delim1.changeVisibility(isVisible)
        tab_delim2.changeVisibility(isVisible)
    }

    // ------------------------------------------
    override fun onTabReselect() {
        super.onTabReselect()
        vp_pages?.let {
            val nextPage = it.currentItem + 1
            selectPage(nextPage.takeIf { it > 2 }?.let { 0 } ?: nextPage)
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lmmPagesAdapter = LmmPagerAdapter(childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.apply {
            observe(vm.badgeLikes, ::showBadgeOnLikes)
            observe(vm.badgeMatches, ::showBadgeOnMatches)
            observe(vm.badgeMessenger, ::showBadgeOnMessenger)
        }
        selectPage(position = 2)  // open LikesYou at beginning
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.apply {
            adapter = lmmPagesAdapter
            offscreenPageLimit = 3
        }
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { selectPage(2) }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { selectPage(1) }
        btn_tab_messenger.clicks().compose(clickDebounce()).subscribe { selectPage(0) }
    }

    private fun selectPage(position: Int) {
        when (position) {
            0 -> {
                lmmPagesAdapter.accessItem(1)?.userVisibleHint = false
                lmmPagesAdapter.accessItem(2)?.userVisibleHint = false
                btn_tab_likes?.changeTypeface()
                btn_tab_matches?.changeTypeface()
                btn_tab_messenger?.changeTypeface(style = Typeface.BOLD, isSelected = true)
            }
            1 -> {
                lmmPagesAdapter.accessItem(0)?.userVisibleHint = false
                lmmPagesAdapter.accessItem(2)?.userVisibleHint = false
                btn_tab_likes?.changeTypeface()
                btn_tab_matches?.changeTypeface(style = Typeface.BOLD, isSelected = true)
                btn_tab_messenger?.changeTypeface()
            }
            2 -> {
                lmmPagesAdapter.accessItem(0)?.userVisibleHint = false
                lmmPagesAdapter.accessItem(1)?.userVisibleHint = false
                btn_tab_likes?.changeTypeface(style = Typeface.BOLD, isSelected = true)
                btn_tab_matches?.changeTypeface()
                btn_tab_messenger?.changeTypeface()
            }
        }

        if (vp_pages?.currentItem == position) {
            // current position reselected
            vm.onTabReselect()
        }

        lmmPagesAdapter.accessItem(position)?.userVisibleHint = true
        vp_pages?.setCurrentItem(position, false)
    }
}
