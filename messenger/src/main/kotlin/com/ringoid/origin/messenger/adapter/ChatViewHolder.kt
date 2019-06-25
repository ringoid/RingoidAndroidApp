package com.ringoid.origin.messenger.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.messenger.Message
import kotlinx.android.synthetic.main.rv_item_chat_item.view.*

abstract class BaseChatViewHolder(view: View) : BaseViewHolder<Message>(view) {

    override fun bind(model: Message) {
        itemView.tv_chat_message.text = model.text
//        itemView.tv_chat_message.text = "${model.text}(${model.id.substring(0..3)}, ${model.clientId.substring(0..3)})"
    }
}

class MyChatViewHolder(view: View) : BaseChatViewHolder(view)

class PeerChatViewHolder(view: View) : BaseChatViewHolder(view)

class HeaderChatViewHolder(view: View) : BaseChatViewHolder(view) {

    override fun bind(model: Message) {
        // no-op
    }
}
