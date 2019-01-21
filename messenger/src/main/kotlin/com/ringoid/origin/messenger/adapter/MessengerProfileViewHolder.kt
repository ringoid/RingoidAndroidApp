package com.ringoid.origin.messenger.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.FeedViewHolder

class MessengerProfileViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : FeedViewHolder(view, viewPool) {

    override fun bind(model: Profile) {
        super.bind(model)
    }
}
