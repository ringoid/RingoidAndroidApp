package com.ringoid.origin.feed.view.lc.like

import android.os.Bundle
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowLikeBtnOnScroll
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment
import com.ringoid.origin.feed.view.lmm.TRANSFER_PROFILE
import com.ringoid.origin.feed.view.lmm.base.PUSH_NEW_LIKES_TOTAL
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.IBaseMainActivity
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.origin.view.particles.PARTICLE_TYPE_LIKE
import com.ringoid.utility.communicator
import com.ringoid.utility.image.ImageRequest

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

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            ViewState.CLEAR.MODE_CHANGE_FILTERS -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_empty_no_data_filters)
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
