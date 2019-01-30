package com.ringoid.origin.feed.view.explore

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.OriginR_string
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.FeedFragment
import com.ringoid.origin.view.common.EmptyFragment
import timber.log.Timber

class ExploreFragment : FeedFragment<ExploreViewModel, Profile, OriginFeedViewHolder<Profile>>() {

    companion object {
        fun newInstance(): ExploreFragment = ExploreFragment()
    }

    override fun createFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool?)
            : BaseFeedAdapter<Profile, OriginFeedViewHolder<Profile>> =
        FeedAdapter(imagesViewPool).apply {
            onLikeImageListener = { model: ProfileImageVO, _ ->
                Timber.v("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
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
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (isActivityCreated && hidden) {
            vm.purgeAlreadySeenProfiles()
        }
    }

    // ------------------------------------------
    override fun onTabReselect() {
        super.onTabReselect()
        scrollToTopOfItemAtPosition(0)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewLifecycleOwner.observe(vm.feed) { feedAdapter.append(it.profiles) { !isEmpty() } }
        vm.clearScreen(mode = ViewState.CLEAR.MODE_NEED_REFRESH)
    }
}
