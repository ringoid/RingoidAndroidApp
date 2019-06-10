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
import com.ringoid.origin.view.particles.PARTICLE_TYPE_LIKE
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MATCH
import com.ringoid.origin.view.particles.PARTICLE_TYPE_MESSAGE
import com.ringoid.utility.changeTypeface
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
        fun clearScreen(mode: Int) {
            lmmPagesAdapter.doForEachItem { (it as? BaseLmmFeedFragment<*>)?.clearScreen(mode) }
        }

        fun showLoading(isVisible: Boolean) {
            lmmPagesAdapter.doForEachItem { (it as? BaseLmmFeedFragment<*>)?.showLoading(isVisible) }
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.CLEAR -> clearScreen(mode = newState.mode)
            is ViewState.IDLE -> showLoading(isVisible = false)
            is ViewState.LOADING -> showLoading(isVisible = true)
            is ViewState.DONE -> {
                when (newState.residual) {
                    /**
                     * When user manually pulls to refresh on some of Lmm feeds, it gets cleared and
                     * refresh spinner is showing, and the whole Lmm data is fetching. Once completes,
                     * all Lmm feeds will be updated, so need to programmatically clear other Lmm feeds
                     * and display refresh spinner on them to avoid user to navigate on them and then
                     * lost some of his actions when Lmm data received.
                     */
                    is CLEAR_AND_REFRESH_EXCEPT -> {
                        val exceptLmmTab = (newState.residual as CLEAR_AND_REFRESH_EXCEPT).exceptLmmTab
                        LmmNavTab.values.forEach { lmmTab ->
                            when (lmmTab) {
                                exceptLmmTab -> { /* ignore except lmm tab */ }
                                else -> lmmPagesAdapter.accessItem(lmmTab)
                                    ?.let { it as? BaseLmmFeedFragment<*> }
                                    ?.let {
                                        it.clearScreen(ViewState.CLEAR.MODE_DEFAULT)
                                        it.showLoading(isVisible = true)
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun accessViewModel(): LmmViewModel = vm

    // ------------------------------------------
    override fun showBadgeOnLikes(isVisible: Boolean) {
        btn_tab_likes.showBadge(isVisible)
        hasAnyBadgeShown()
    }

    override fun showBadgeOnMatches(isVisible: Boolean) {
        btn_tab_matches.showBadge(isVisible)
        hasAnyBadgeShown()
    }

    override fun showBadgeOnMessenger(isVisible: Boolean) {
        btn_tab_messenger.showBadge(isVisible)
        hasAnyBadgeShown()
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
            observe(vm.pushNewLike) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_LIKE) }
            observe(vm.pushNewMatch) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_MATCH) }
            observe(vm.pushNewMessage) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_MESSAGE) }
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
