package com.ringoid.origin.feed.adapter.lmm

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderHideChatControls
import com.ringoid.origin.feed.adapter.base.LikeFeedViewHolderShowChatControls
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class LikeFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItemVO) {
        super.bind(model)
        itemView.ibtn_message.changeVisibility(isVisible = false)  // hide message button initially
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(LikeFeedViewHolderHideChatControls)) {
            itemView.ibtn_message.changeVisibility(isVisible = false)
        }
        if (payloads.contains(LikeFeedViewHolderShowChatControls)) {
            itemView.ibtn_message.changeVisibility(isVisible = true)
        }
        super.bind(model, payloads)
    }
}
