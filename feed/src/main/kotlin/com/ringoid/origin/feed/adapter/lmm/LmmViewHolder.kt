package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder

interface ILmmViewHolder

abstract class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : OriginFeedViewHolder<FeedItem>(view, viewPool)

abstract class BaseLmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder<FeedItem>(view, viewPool)

open class HeaderLmmViewHolder(view: View) : BaseViewHolder<FeedItem>(view), ILmmViewHolder {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
