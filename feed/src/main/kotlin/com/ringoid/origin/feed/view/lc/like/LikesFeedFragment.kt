package com.ringoid.origin.feed.view.lc.like

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.WidgetR_attrs
import com.ringoid.origin.feed.WidgetR_color
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment
import com.ringoid.origin.feed.view.lmm.LC_FEED_COUNTS
import com.ringoid.origin.feed.view.lmm.TRANSFER_PROFILE
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_LIKES_TOTAL
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.filters.BaseFiltersFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.origin.view.particles.PARTICLE_TYPE_LIKE
import com.ringoid.utility.communicator
import com.ringoid.utility.getAttributeColor
import com.ringoid.utility.image.ImageRequest
import kotlinx.android.synthetic.main.dialog_filters.*
import kotlinx.android.synthetic.main.fragment_feed.*

class LikesFeedFragment : BaseLcFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun createFeedAdapter(): BaseLmmAdapter =
        LikeFeedAdapter(ImageRequest(context!!)).apply {
            onLikeImageListener = { model: ProfileImageVO, _ /** feed item position */: Int ->
                if (!connectionManager.isNetworkAvailable()) {
                    noConnection(this@LikesFeedFragment)
                } else {
                    vm.onLike(profileId = model.profileId, imageId = model.image.id)
                }
            }
        }

    override fun createFiltersFragment(): BaseFiltersFragment<*> = LikesFeedFiltersFragment.newInstance()

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            ViewState.CLEAR.MODE_CHANGE_FILTERS ->
                EmptyFragment.Companion.Input(
                    emptyTextResId = OriginR_string.feed_empty_no_data_filters,
                    labelTextColor = context?.getAttributeColor(WidgetR_attrs.refTextColorPrimary) ?: ContextCompat.getColor(context!!, WidgetR_color.primary_text),
                    isLabelClickable = true)
            else -> null
        }

    override fun getSourceFeed(): LcNavTab = LcNavTab.LIKES

    override fun getToolbarTitleResId(): Int = OriginR_string.feed_likes_you_title

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is LC_FEED_COUNTS ->
                        (newState.residual as LC_FEED_COUNTS).let {
                            setToolbarTitleWithLcCounts(show = it.show, hidden = it.hidden)
                        }
                    is TRANSFER_PROFILE -> {
                        val profileId = (newState.residual as TRANSFER_PROFILE).profileId
                        onDiscardProfileState(profileId)?.let { discarded ->
                            communicator(IBaseMainActivity::class.java)?.let {
                                val payload = Bundle().apply {
                                    putInt("positionOfImage", discarded.positionOfImage)
                                    putSerializable("destinationFeed", LcNavTab.MESSAGES)
                                }
                                Bus.post(BusEvent.TransferProfile(profileId = discarded.id, payload = payload))
                            }
                        }
                    }
                    is PUSH_NEW_LIKES_TOTAL -> communicator(IBaseMainActivity::class.java)?.showBadgeOnLikes(isVisible = true)
                }
            }
        }
    }

    override fun setDefaultToolbarTitle() {
        toolbar.setTitle(OriginR_string.feed_likes_you_title)
    }

    override fun setToolbarTitleWithLcCounts(show: Int, hidden: Int) {
        super.setToolbarTitleWithLcCounts(show, hidden)
        toolbar.title = if (hidden > 0) String.format(AppRes.LC_TITLE_LIKES_HIDDEN, show, hidden)
                        else String.format(AppRes.LC_TITLE_LIKES, show)
    }

    // ------------------------------------------
    override fun setCountOfFilteredFeedItems(count: Int) {
        btn_apply_filters.text = String.format(AppRes.FILTER_BUTTON_APPLY, count)
    }

    override fun setTotalNotFilteredFeedItems(count: Int) {
        btn_show_all.text = String.format(AppRes.FILTER_BUTTON_SHOW_ALL, count)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with(viewLifecycleOwner) {
            observe(vm.pushNewLike) { communicator(IBaseMainActivity::class.java)?.showParticleAnimation(PARTICLE_TYPE_LIKE) }
        }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(tag = "like btn bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideLikeBtnOnScroll, show = FeedViewHolderShowLikeBtnOnScroll))
            }
}
