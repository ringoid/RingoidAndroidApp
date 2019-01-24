package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.base.BaseFeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import timber.log.Timber

class ExploreFragment : BaseFeedFragment<ExploreViewModel, Profile, OriginFeedViewHolder<Profile>>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun createFeedAdapter(): BaseFeedAdapter<Profile, OriginFeedViewHolder<Profile>> =
        FeedAdapter().apply {
            onLikeImageListener = { model: ProfileImageVO, _ ->
                Timber.i("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked)
            }
        }

    override fun getVmClass(): Class<ExploreViewModel> = ExploreViewModel::class.java

    override fun getEmptyStateInput(mode: Int): EmptyFragment.Companion.Input? =
        when (mode) {
            ViewState.CLEAR.MODE_EMPTY_DATA -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_explore_empty_no_data)
            ViewState.CLEAR.MODE_NEED_REFRESH -> EmptyFragment.Companion.Input(emptyTextResId = OriginR_string.feed_explore_empty_need_refresh)
            else -> null
        }

    // --------------------------------------------------------------------------------------------
    override fun onTabReselect() {
        super.onTabReselect()
        scrollToTopOfItemAtPosition(0)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed) { feedAdapter.submitList(it.profiles) }
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }
}
