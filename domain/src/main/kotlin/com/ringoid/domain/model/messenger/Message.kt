package com.ringoid.domain.model.messenger

import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.IListModel
import com.ringoid.utility.randomLong

data class Message(val id: Long = randomLong(), val peerId: String, val text: String) : IListModel {

    override fun getModelId(): Long = id
}

val EmptyMessage = Message(id = randomLong(), peerId = BAD_ID, text = "")
