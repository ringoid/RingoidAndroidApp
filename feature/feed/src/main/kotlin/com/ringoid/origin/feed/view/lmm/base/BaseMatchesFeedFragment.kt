package com.ringoid.origin.feed.view.lmm.base

import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowControls
import com.ringoid.origin.feed.misc.OffsetScrollStrategy

@Deprecated("LMM -> LC")
abstract class BaseMatchesFeedFragment<VM : BaseLmmFeedViewModel> : BaseLmmFeedFragment<VM>() {

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is PUSH_NEW_MESSAGES -> {
                        val profileId = (newState.residual as PUSH_NEW_MESSAGES).profileId
                        feedAdapter.findPosition { it.id == profileId }
                            .takeIf { it != DomainUtil.BAD_POSITION }
                            ?.let { feedAdapter.notifyItemChanged(it, FeedViewHolderShowControls) }
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
                add(OffsetScrollStrategy(tag = "bias btn bottom", type = OffsetScrollStrategy.Type.BOTTOM, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_BOTTOM, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
                add(OffsetScrollStrategy(tag = "bias btn top", type = OffsetScrollStrategy.Type.TOP, deltaOffset = AppRes.FEED_ITEM_BIAS_BTN_TOP, hide = FeedViewHolderHideChatBtnOnScroll, show = FeedViewHolderShowChatBtnOnScroll))
            }
}
