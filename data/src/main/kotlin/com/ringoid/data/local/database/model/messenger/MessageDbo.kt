package com.ringoid.data.local.database.model.messenger

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.messenger.IMessage
import com.ringoid.domain.model.messenger.Message

@Entity(tableName = MessageDbo.TABLE_NAME)
data class MessageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_CHAT_ID) val chatId: String,  // same as peerId (profileId)
    @ColumnInfo(name = COLUMN_CLIENT_ID) val clientId: String,
    @ColumnInfo(name = COLUMN_PEER_ID) val peerId: String,  // can be either profileId or [DomainUtil.CURRENT_USER_ID]
    @Deprecated("Unsupported")
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String = "",  // deprecated, left to avoid migration
    @ColumnInfo(name = COLUMN_TEXT) override val text: String,
    @ColumnInfo(name = COLUMN_TIMESTAMP) val ts: Long,
    @ColumnInfo(name = COLUMN_UNREAD) val unread: Int = 1)
    : Mappable<Message>, IMessage {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_CHAT_ID = "chatId"
        const val COLUMN_CLIENT_ID = "clientId"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_SOURCE_FEED = "sourceFeed"  // deprecated, left to avoid migration
        const val COLUMN_TEXT = "text"
        const val COLUMN_TIMESTAMP = "ts"
        const val COLUMN_UNREAD = "unread"

        const val TABLE_NAME = "Messages"

        fun from(message: Message, unread: Int = 1): MessageDbo =
            MessageDbo(
                id = message.id,
                chatId = message.chatId,
                clientId = message.clientId,
                peerId = message.peerId,
                text = message.text,
                ts = message.ts,
                unread = unread)
    }

    override fun map(): Message =
        Message(id = id, chatId = chatId, clientId = clientId, peerId = peerId, text = text, ts = ts)
}
