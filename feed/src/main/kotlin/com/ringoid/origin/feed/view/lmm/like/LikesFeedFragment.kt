package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.*
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import timber.log.Timber

class LikesFeedFragment : BaseLmmFeedFragment<LikesFeedViewModel>() {

    companion object {
        fun newInstance(): LikesFeedFragment = LikesFeedFragment()
    }

    override fun getVmClass(): Class<LikesFeedViewModel> = LikesFeedViewModel::class.java

    override fun instantiateFeedAdapter(): BaseLmmAdapter =
        LikeFeedAdapter().apply {
            onLikeImageListener = { model: ProfileImageVO, feedItemPosition: Int ->
                Timber.v("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked, feedItemPosition = feedItemPosition)
            }
        }

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_likes_you_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.common_pull_to_refresh)
            else -> null
        }

    override fun getSourceFeed(): String = DomainUtil.SOURCE_FEED_LIKES

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is HAS_LIKES_ON_PROFILE -> {
                        val position = (newState.residual as HAS_LIKES_ON_PROFILE).feedItemPosition
                        feedAdapter.notifyItemChanged(position, LikeFeedViewHolderShowChatControls)
                        getStrategyByTag(tag = "chatBottom")?.enableForPosition(position)
                        getStrategyByTag(tag = "chatTop")?.enableForPosition(position)
                    }
                    is NO_LIKES_ON_PROFILE -> {
                        val position = (newState.residual as NO_LIKES_ON_PROFILE).feedItemPosition
                        feedAdapter.notifyItemChanged(position, LikeFeedViewHolderHideChatControls)
                        getStrategyByTag(tag = "chatBottom")?.disableForPosition(position)
                        getStrategyByTag(tag = "chatTop")?.disableForPosition(position)
                    }
                }
            }
        }
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    override fun getOffsetScrollStrategies(): List<OffsetScrollStrategy> =
        mutableListOf<OffsetScrollStrategy>()
            .apply {
                addAll(super.getOffsetScrollStrategies())
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideLikeBtnOnScroll, show = FeedViewHolderShowLikeBtnOnScroll))
                add(OffsetScrollStrategy(tag = "chatBottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_MID_BTN_BOTTOM, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_TABS_INDICATOR_TOP, hide = FeedViewHolderHideTabsIndicatorOnScroll, show = FeedViewHolderShowTabsIndicatorOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_SETTINGS_BTN_TOP, hide = FeedViewHolderHideSettingsBtnOnScroll, show = FeedViewHolderShowSettingsBtnOnScroll))
                add(OffsetScrollStrategy(type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_TOP, hide = FeedViewHolderHideLikeBtnOnScroll, show = FeedViewHolderShowLikeBtnOnScroll))
                add(OffsetScrollStrategy(tag = "chatTop", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_MID_BTN_TOP, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
            }
}
