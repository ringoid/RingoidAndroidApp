package com.ringoid.origin.feed.view.lmm.like

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderHideChatControls
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderShowChatControls
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
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
}
