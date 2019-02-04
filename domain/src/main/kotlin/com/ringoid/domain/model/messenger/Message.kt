package com.ringoid.domain.model.messenger

import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.IListModel
import com.ringoid.utility.randomString

data class Message(val id: String = randomString(), val chatId: String, val peerId: String, val text: String) : IListModel {

    override fun getModelId(): Long = id.hashCode().toLong()
}

val EmptyMessage = Message(id = randomString(), chatId = BAD_ID, peerId = BAD_ID, text = "")
