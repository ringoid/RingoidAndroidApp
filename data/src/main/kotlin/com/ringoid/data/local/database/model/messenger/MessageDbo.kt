package com.ringoid.data.local.database.model.messenger

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.messenger.Message

@Entity(tableName = MessageDbo.TABLE_NAME)
data class MessageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_CHAT_ID) val chatId: String,
    @ColumnInfo(name = COLUMN_PEER_ID) val peerId: String,
    @ColumnInfo(name = COLUMN_TEXT) val text: String,
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES)
    : Mappable<Message> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_CHAT_ID = "chatId"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_TEXT = "text"
        const val COLUMN_SOURCE_FEED = "sourceFeed"

        const val TABLE_NAME = "Messages"

        fun from(message: Message, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): MessageDbo =
            MessageDbo(id = message.id, chatId = message.chatId, peerId = message.peerId, text = message.text, sourceFeed = sourceFeed)
    }

    override fun map(): Message = Message(id = id, chatId = chatId, peerId = peerId, text = text)
}
