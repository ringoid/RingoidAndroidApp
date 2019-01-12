package com.ringoid.origin.view.adapter.feed

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.R

class FeedAdapter(private var viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseListAdapter<Profile, ProfileViewHolder>(ProfileDiffCallback()) {

    init {
        viewPool = viewPool ?: RecyclerView.RecycledViewPool()
    }

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool?)
            : ProfileViewHolder = ProfileViewHolder(view, viewPool)

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
