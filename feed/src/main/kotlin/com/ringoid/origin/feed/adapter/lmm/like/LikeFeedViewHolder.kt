package com.ringoid.origin.feed.adapter.lmm.like

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.BaseLmmViewHolder
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

interface ILikeFeedViewHolder

abstract class OriginLikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool), ILikeFeedViewHolder

abstract class BaseLikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLmmViewHolder(view, viewPool), ILikeFeedViewHolder

class LikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLikeFeedViewHolder(view, viewPool)

class HeaderLikeFeedViewHolder(view: View) : OriginLikeFeedViewHolder(view) {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
