package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.feed.adapter.base.BaseFeedViewHolder
import com.ringoid.origin.feed.adapter.base.OriginFeedViewHolder
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

open class LmmViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder(view, viewPool) {

    init {
        itemView.tv_seen_status.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        super.bind(model)
        itemView.tv_seen_status.text = if (model.isNotSeen) "Not Seen" else "Seen"
    }
}

class HeaderLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

class FooterLmmViewHolder(view: View) : OriginFeedViewHolder(view) {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}
