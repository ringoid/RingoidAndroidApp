package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderHideChatControls
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderShowChatControls
import com.ringoid.origin.feed.model.FeedItemVO

class LikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(LikeFeedViewHolderHideChatControls)) {
            // TODO: hide chat icon
        }
        if (payloads.contains(LikeFeedViewHolderShowChatControls)) {
            // TODO: show chat icon
        }
        super.bind(model, payloads)
    }
}
