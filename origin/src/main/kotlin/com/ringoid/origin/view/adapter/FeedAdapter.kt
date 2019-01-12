package com.ringoid.origin.view.adapter

import android.view.View
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.R

class FeedAdapter : BaseListAdapter<Profile, ProfileViewHolder>(ProfileDiffCallback()) {

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): ProfileViewHolder = ProfileViewHolder(view)

    fun submit(feed: Feed) {
        submitList(feed.profiles)
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileDiffCallback : BaseDiffCallback<Profile>() {

    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem == newItem  // as 'data class'
}
