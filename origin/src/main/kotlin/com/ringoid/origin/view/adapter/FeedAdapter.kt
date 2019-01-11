package com.ringoid.origin.view.adapter

import android.view.ViewGroup
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile

class FeedAdapter : BaseListAdapter<Profile, ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
    }

    fun submit(feed: Feed) {
        submitList(feed.profiles)
    }
}
