package com.ringoid.origin.messenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.EmptyMessage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.messenger.R

class ChatAdapter : OriginListAdapter<Message, BaseChatViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.rv_item_chat_item -> PeerChatViewHolder(view)
            R.layout.rv_item_my_chat_item -> MyChatViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position).peerId) {
            DomainUtil.CURRENT_USER_ID -> R.layout.rv_item_my_chat_item
            else -> R.layout.rv_item_chat_item
        }

    // ------------------------------------------
    override fun getHeaderItem(): Message = EmptyMessage
}
