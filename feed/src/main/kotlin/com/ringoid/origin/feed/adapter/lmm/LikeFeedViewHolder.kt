package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderHideChatControls
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderShowChatControls
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItem) {
        itemView.ibtn_message.changeVisibility(isVisible = false)  // hide message button initially
        super.bind(model)
    }

    override fun bind(model: FeedItem, payloads: List<Any>) {
        if (payloads.contains(LikeFeedViewHolderHideChatControls)) {
            itemView.ibtn_message.changeVisibility(isVisible = false)
        }
        if (payloads.contains(LikeFeedViewHolderShowChatControls)) {
            itemView.ibtn_message.changeVisibility(isVisible = true)
        }
        super.bind(model, payloads)
    }
}
