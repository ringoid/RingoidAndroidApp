package com.ringoid.origin.feed.adapter.lmm.like

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

open class LikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool)

class HeaderLikeFeedViewHolder(view: View) : LikeFeedViewHolder(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
