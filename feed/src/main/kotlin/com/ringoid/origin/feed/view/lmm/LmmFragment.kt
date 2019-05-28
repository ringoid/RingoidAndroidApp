package com.ringoid.origin.feed.view.lmm

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.view.main.BaseMainActivity
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LmmNavTab
import com.ringoid.utility.changeTypeface
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.communicator
import kotlinx.android.synthetic.main.fragment_lmm.*

class LmmFragment : BaseFragment<LmmViewModel>(), ILmmFragment {

    companion object {
        private const val BUNDLE_KEY_CURRENT_PAGE = "bundle_key_current_page"

        fun newInstance(): LmmFragment = LmmFragment()
    }

    private lateinit var lmmPagesAdapter: LmmPagerAdapter

    override fun getVmClass(): Class<LmmViewModel> = LmmViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_lmm

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun showLoading(isVisible: Boolean) {
            lmmPagesAdapter.doForEachItem { (it as? BaseLmmFeedFragment<*>)?.showLoading(isVisible) }
        }

        super.onViewStateChange(newState)
        when (newState) {
            ViewState.IDLE -> showLoading(isVisible = false)
            ViewState.LOADING -> showLoading(isVisible = true)
        }
    }

    // --------------------------------------------------------------------------------------------
    // TODO: save that fields onSaveInstanceState() for later restore
    private var badge_likes_visibilityPrev: Boolean = false
    private var badge_matches_visibilityPrev: Boolean = false
    private var badge_messages_visibilityPrev: Boolean = false

    override fun accessViewModel(): LmmViewModel = vm

    // ------------------------------------------
    override fun showBadgeOnLikes(isVisible: Boolean) {
        badge_likes_visibilityPrev = isVisible
        btn_tab_likes.showBadge(isVisible)
        hasAnyBadgeShown()
    }

    override fun showBadgeOnMatches(isVisible: Boolean) {
        badge_matches_visibilityPrev = isVisible
        btn_tab_matches.showBadge(isVisible)
        hasAnyBadgeShown()
    }

    override fun showBadgeOnMessenger(isVisible: Boolean) {
        badge_messages_visibilityPrev = isVisible
        btn_tab_messenger.showBadge(isVisible)
        hasAnyBadgeShown()
    }

    override fun showTabs(isVisible: Boolean) {
        if (ll_tabs_container == null) {
            return  // view has not been initialized yet
        }

        if (isVisible) {
            btn_tab_likes.showBadge(badge_likes_visibilityPrev)
            btn_tab_matches.showBadge(badge_matches_visibilityPrev)
            btn_tab_messenger.showBadge(badge_messages_visibilityPrev)
        } else {
            btn_tab_likes.showBadge(false)
            btn_tab_matches.showBadge(false)
            btn_tab_messenger.showBadge(false)
        }
        btn_tab_likes.changeVisibility(isVisible)
        btn_tab_matches.changeVisibility(isVisible)
        btn_tab_messenger.changeVisibility(isVisible)
        tab_delim1.changeVisibility(isVisible)
        tab_delim2.changeVisibility(isVisible)
    }

    // ------------------------------------------
    override fun transferProfile(profileId: String, destinationFeed: LmmNavTab) {
        lmmPagesAdapter.accessItem(destinationFeed)
            ?.let { it as? BaseLmmFeedFragment<*> }
            ?.transferProfile(profileId, destinationFeed, payload = null)
    }

    override fun transferProfile(discarded: FeedItemVO?, destinationFeed: LmmNavTab) {
        if (discarded == null) {
            return
        }

        val payload = Bundle().apply { putInt("positionOfImage", discarded.positionOfImage) }
        lmmPagesAdapter.accessItem(destinationFeed)
            ?.let { it as? BaseLmmFeedFragment<*> }
            ?.transferProfile(discarded.id, destinationFeed, payload = payload)
    }

    // ------------------------------------------
    private fun clearAllFeeds(mode: Int) {
        lmmPagesAdapter.doForEachItem { (it as? BaseLmmFeedFragment<*>)?.clearScreen(mode) }
    }

    private fun hasAnyBadgeShown() {
        communicator(IBaseMainActivity::class.java)
            ?.showBadgeOnLmm(isVisible = btn_tab_likes.isBadgeVisible() || btn_tab_matches.isBadgeVisible() || btn_tab_messenger.isBadgeVisible())
    }

    // ------------------------------------------
    private var postponedTabTransaction = false

    override fun onBeforeTabSelect() {
        super.onBeforeTabSelect()
        setCurrentPageVisibleHint(false)
    }

    override fun onTabReselect(payload: String?) {
        super.onTabReselect(payload)
        if (!isViewModelInitialized) {
            return
        }

        payload?.let { lmmFeedName -> selectPage(LmmNavTab.from(lmmFeedName)?.page() ?: 0) }
    }

    override fun onTabTransaction(payload: String?) {
        super.onTabTransaction(payload)
        if (!isViewModelInitialized) {
            postponedTabTransaction = true
            return
        }

        payload?.let { lmmFeedName -> selectPage(LmmNavTab.from(lmmFeedName)?.page() ?: 0) }

        setCurrentPageVisibleHint(true)
        showTabs(isVisible = true)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lmmPagesAdapter = LmmPagerAdapter(childFragmentManager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.badgeLikes, ::showBadgeOnLikes)
            observe(vm.badgeMatches, ::showBadgeOnMatches)
            observe(vm.badgeMessenger, ::showBadgeOnMessenger)
            observe(vm.clearAllFeeds, ::clearAllFeeds)
        }

        val page = savedInstanceState?.getInt(BUNDLE_KEY_CURRENT_PAGE) ?: 0
        selectPage(position = page)  // open LikesYou at beginning

        if (postponedTabTransaction) {
            doPostponedTabTransaction()
            postponedTabTransaction = false
        }
    }

    override fun onActivitySaveInstanceState(outState: Bundle) {
        super.onActivitySaveInstanceState(outState)
        vp_pages?.currentItem?.let { position ->
            outState.putSerializable(BaseMainActivity.BUNDLE_KEY_CURRENT_LMM_TAB, LmmNavTab.get(position))
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vp_pages.apply {
            adapter = lmmPagesAdapter
            offscreenPageLimit = 3
        }
        btn_tab_likes.clicks().compose(clickDebounce()).subscribe { selectPage(0) }
        btn_tab_matches.clicks().compose(clickDebounce()).subscribe { selectPage(1) }
        btn_tab_messenger.clicks().compose(clickDebounce()).subscribe { selectPage(2) }
    }

    private fun selectPage(position: Int) {
        when (position) {
            0 -> {
                btn_tab_likes?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
            }
            1 -> {
                btn_tab_likes?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
            }
            2 -> {
                btn_tab_likes?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_matches?.changeTypeface(textSize = AppRes.BUTTON_FLAT_TEXT_SIZE)
                btn_tab_messenger?.changeTypeface(style = Typeface.BOLD, isSelected = true, textSize = AppRes.BUTTON_FLAT_INC_TEXT_SIZE)
            }
        }

        vp_pages?.currentItem?.let {
            if (it != position) {
                setPageVisibleHint(it, false)
                setPageVisibleHint(position, true)
                vp_pages?.setCurrentItem(position, false)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_KEY_CURRENT_PAGE, vp_pages?.currentItem ?: 0)
    }

    // ------------------------------------------
    private fun setPageVisibleHint(position: Int, hint: Boolean) {
        lmmPagesAdapter.accessItem(position)?.userVisibleHint = hint
    }

    private fun setCurrentPageVisibleHint(hint: Boolean) {
        vp_pages?.let { lmmPagesAdapter.accessItem(it.currentItem)?.userVisibleHint = hint }
    }
}
