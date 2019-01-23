package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

open class MatchFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool)

class HeaderMatchFeedViewHolder(view: View) : MatchFeedViewHolder(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
