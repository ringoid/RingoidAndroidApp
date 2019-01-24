package com.ringoid.origin.messenger.adapter

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.messenger.Message

class MessageDiffCallback : BaseDiffCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem == newItem  // as 'data class'
}
