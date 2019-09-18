package com.ringoid.domain.model.messenger

const val READ_BY_PEER = 2
const val READ_BY_USER = 0
const val UNREAD_BY_PEER = 3
const val UNREAD_BY_USER = 1

enum class MessageReadStatus(val value: Int) {
    ReadByPeer(READ_BY_PEER),
    ReadByUser(READ_BY_USER),
    UnreadByPeer(UNREAD_BY_PEER),
    UnreadByUser(UNREAD_BY_USER);

    companion object {
        private val values = values().associateBy(MessageReadStatus::value)
        fun from(value: Int): MessageReadStatus = values[value] ?: ReadByUser
    }
}
