package com.ringoid.origin.feed.view.lmm.like

import android.os.Bundle
import android.view.View
import com.ringoid.base.view.ViewState
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.FeedViewHolderHideChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.FeedViewHolderShowChatBtnOnScroll
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderHideChatControls
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderShowChatControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lmm.ILmmFragment
import com.ringoid.origin.feed.view.lmm.base.BaseLmmFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.utility.communicator
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

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is HAS_LIKES_ON_PROFILE -> {
                        val position = (newState.residual as HAS_LIKES_ON_PROFILE).feedItemPosition
                        feedAdapter.notifyItemChanged(position, LikeFeedViewHolderShowChatControls)
                    }
                    is NO_LIKES_ON_PROFILE -> {
                        val position = (newState.residual as NO_LIKES_ON_PROFILE).feedItemPosition
                        feedAdapter.notifyItemChanged(position, LikeFeedViewHolderHideChatControls)
                    }
                }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        communicator(ILmmFragment::class.java)?.accessViewModel()
            ?.cachedLmm?.likes?.let { feedAdapter.submitList(it) }
    }

    // --------------------------------------------------------------------------------------------
    override fun onRefresh() {
        super.onRefresh()
        chatBtnHide = false
        chatBtnShow = false
        likeBtnHide = false
        likeBtnShow = false
    }

    /* Scroll listeners */
    // --------------------------------------------------------------------------------------------
    protected val CHAT_BTN_BOTTOM = AppRes.FEED_ITEM_MID_BTN_TOP_OFFSET
    protected var chatBtnHide: Boolean = false
    protected var chatBtnShow: Boolean = false

    override fun processItemViewControlVisibility(position: Int, view: View) {
        super.processItemViewControlVisibility(position, view)
        if (BB_TOP - view.top >= CHAT_BTN_BOTTOM) {
            chatBtnHide = false
//            if (!chatBtnShow) {
                chatBtnShow = true
                feedAdapter.notifyItemChanged(position, FeedViewHolderShowChatBtnOnScroll)
//            }
        } else {
            chatBtnShow = false
//            if (!chatBtnHide) {
                chatBtnHide = true
                feedAdapter.notifyItemChanged(position, FeedViewHolderHideChatBtnOnScroll)
//            }
        }
    }
}
