package com.ringoid.domain.model.messenger

const val READ_BY_PEER = 0
const val READ_BY_USER = 1
const val UNREAD_BY_PEER = 2
const val UNREAD_BY_USER = 3

enum class MessageReadStatus(val value: Int) {
    // TODO: legacy apps have 0 and 1 for 'unread' in Db
    ReadByPeer(READ_BY_PEER),
    ReadByUser(READ_BY_USER),
    UnreadByPeer(UNREAD_BY_PEER),
    UnreadByUser(UNREAD_BY_USER);

    fun convertToReadByUser(convert: Boolean): MessageReadStatus =
        if (convert) {
            when (this) {
                UnreadByUser -> ReadByUser
                else -> this
            }
        } else this  // do not convert

    companion object {
        private val values = values().associateBy(MessageReadStatus::value)
        fun from(value: Int): MessageReadStatus = values[value] ?: ReadByUser
    }
}
