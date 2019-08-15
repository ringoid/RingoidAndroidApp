package com.ringoid.origin.messenger.adapter

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.messenger.Message

class MessageDiffCallback : BaseDiffCallback<Message>() {

    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem.id == newItem.id ||
        oldItem.id == newItem.clientId ||
        (oldItem.clientId.isNotBlank() && oldItem.clientId == newItem.clientId)

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
        oldItem.text == newItem.text  // as 'data class'
}
