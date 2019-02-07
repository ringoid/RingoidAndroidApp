package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

class MessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItem) {
        super.bind(model)
    }
}
