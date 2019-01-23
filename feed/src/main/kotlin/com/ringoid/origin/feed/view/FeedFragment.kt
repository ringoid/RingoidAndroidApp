package com.ringoid.origin.feed.view

import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.base.BaseFeedAdapter
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.base.BaseFeedFragment
import timber.log.Timber

abstract class FeedFragment<VM : FeedViewModel> : BaseFeedFragment<VM, Profile, OriginFeedViewHolder<Profile>>() {

    override fun createFeedAdapter(): BaseFeedAdapter<Profile, OriginFeedViewHolder<Profile>> =
        FeedAdapter().apply {
            onLikeImageListener = { model: ProfileImageVO, _ ->
                Timber.i("${if (model.isLiked) "L" else "Unl"}iked image: ${model.image}")
                vm.onLike(profileId = model.profileId, imageId = model.image.id, isLiked = model.isLiked)
            }
        }
}
