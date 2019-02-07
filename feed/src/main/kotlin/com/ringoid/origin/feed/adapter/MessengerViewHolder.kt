package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder
import kotlinx.android.synthetic.main.rv_item_lmm_profile.view.*

class MessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool) {

    override fun bind(model: FeedItem) {
        super.bind(model)
        setMessengerIcon(model)
    }

    override fun bind(model: FeedItem, payloads: List<Any>) {
        super.bind(model, payloads)
        setMessengerIcon(model)
    }

    // --------------------------------------------------------------------------------------------
    private fun setMessengerIcon(model: FeedItem) {
        val iconResId = if (model.messages.isEmpty()) {
            R.drawable.ic_chat_bubble_outline_white_36dp
        } else if (model.hasPeerMessages()) {
            if (model.messages.size == ChatInMemoryCache.getMessagesCount(model.id)) {
                R.drawable.ic_messenger_outline_white_36dp
            } else {  // has unread messages
                // TODO: maybe check whether peer's count changed, not total
                R.drawable.ic_messenger_fill_lgreen_36dp
            }
        } else {  // contains only current user's messages
            R.drawable.ic_chat_bubble_white_36dp
        }
        itemView.ibtn_message.setImageResource(resId = iconResId)
    }
}
