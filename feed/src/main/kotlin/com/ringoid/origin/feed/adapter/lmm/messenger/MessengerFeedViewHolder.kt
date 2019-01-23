package com.ringoid.origin.feed.adapter.lmm.messenger

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

open class MessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool)

class HeaderMessengerViewHolder(view: View) : MessengerViewHolder(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}