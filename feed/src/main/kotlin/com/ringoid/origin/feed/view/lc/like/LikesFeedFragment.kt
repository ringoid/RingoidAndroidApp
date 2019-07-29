package com.ringoid.origin.feed.view.lc.like

import com.ringoid.base.view.ViewState
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.lmm.BaseLmmAdapter
import com.ringoid.origin.feed.adapter.lmm.LikeFeedAdapter
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedFragment
import com.ringoid.origin.navigation.noConnection
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.main.LmmNavTab
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
            else -> null
        }

    override fun getSourceFeed(): LmmNavTab = LmmNavTab.LIKES

    // --------------------------------------------------------------------------------------------
}
