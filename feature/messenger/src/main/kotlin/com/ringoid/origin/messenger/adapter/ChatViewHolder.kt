package com.ringoid.origin.messenger.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.DebugOnly
import kotlinx.android.synthetic.main.rv_item_chat_item.view.*

abstract class BaseChatViewHolder(view: View) : BaseViewHolder<Message>(view) {

    override fun bind(model: Message) {
        itemView.tv_chat_message.text = model.text
    }
}

@DebugOnly
abstract class DebugBaseChatViewHolder(view: View) : BaseChatViewHolder(view) {

    override fun bind(model: Message) {
        itemView.tv_chat_message.text = "${model.text}(${model.id.substring(0..3)}, ${model.clientId.substring(0..3)})"
    }
}

class MyChatViewHolder(view: View) : BaseChatViewHolder(view)
@DebugOnly
class DebugMyChatViewHolder(view: View) : DebugBaseChatViewHolder(view)

class PeerChatViewHolder(view: View) : BaseChatViewHolder(view)
@DebugOnly
class DebugPeerChatViewHolder(view: View) : DebugBaseChatViewHolder(view)

class HeaderChatViewHolder(view: View) : BaseChatViewHolder(view) {

    override fun bind(model: Message) {
        // no-op
    }
}
