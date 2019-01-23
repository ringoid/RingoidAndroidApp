package com.ringoid.origin.feed.adapter.lmm.match

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.BaseLmmViewHolder
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

interface IMatchFeedViewHolder

abstract class OriginMatchFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool)

abstract class BaseMatchFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLmmViewHolder(view, viewPool)

class MatchFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseMatchFeedViewHolder(view, viewPool)

class HeaderMatchFeedViewHolder(view: View) : OriginMatchFeedViewHolder(view), IMatchFeedViewHolder {

    override fun bind(model: FeedItem, payloads: List<Any>) {
        // no-op
    }
}
