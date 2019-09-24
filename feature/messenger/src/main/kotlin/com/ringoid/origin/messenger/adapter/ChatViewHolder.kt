package com.ringoid.origin.messenger.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.messenger.MessageReadStatus
import com.ringoid.origin.messenger.R
import com.ringoid.utility.DebugOnly
import kotlinx.android.synthetic.main.rv_item_chat_item.view.tv_chat_message
import kotlinx.android.synthetic.main.rv_item_my_chat_item.view.*

abstract class BaseChatViewHolder(view: View) : BaseViewHolder<Message>(view) {

    override fun bind(model: Message) {
        itemView.tv_chat_message.text = model.text
    }
}

@DebugOnly
abstract class DebugBaseChatViewHolder(view: View) : BaseChatViewHolder(view) {

    @Suppress("SetTextI18n")
    override fun bind(model: Message) {
        itemView.tv_chat_message.text = "${model.text}(${model.id.substring(0..3)}, ${model.clientId.substring(0..3)})"
    }
}

class MyChatViewHolder(view: View) : BaseChatViewHolder(view) {

    override fun bind(model: Message) {
        super.bind(model)
        val iconResId = when (model.readStatus) {
            MessageReadStatus.ReadByPeer -> R.drawable.ic_chat_message_read_green_shadow_18dp
            MessageReadStatus.UnreadByPeer -> R.drawable.ic_chat_message_sent_white_shadow_18dp
            else -> R.drawable.ic_empty_stub_18dp
        }
        itemView.iv_chat_message_read_status.setImageResource(iconResId)
    }
}

@DebugOnly
class DebugMyChatViewHolder(view: View) : DebugBaseChatViewHolder(view) {

    override fun bind(model: Message) {
        super.bind(model)
        val iconResId = when (model.readStatus) {
            MessageReadStatus.ReadByPeer -> R.drawable.ic_chat_message_read_green_shadow_18dp
            MessageReadStatus.UnreadByPeer -> R.drawable.ic_chat_message_sent_white_shadow_18dp
            else -> R.drawable.ic_empty_stub_18dp
        }
        itemView.iv_chat_message_read_status.setImageResource(iconResId)
    }

}

class PeerChatViewHolder(view: View) : BaseChatViewHolder(view)

@DebugOnly
class DebugPeerChatViewHolder(view: View) : DebugBaseChatViewHolder(view)

class HeaderChatViewHolder(view: View) : BaseChatViewHolder(view) {

    override fun bind(model: Message) {
        // no-op
    }
}
