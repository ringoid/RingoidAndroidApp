package com.ringoid.data.local.database.model.messenger

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.messenger.IMessage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.messenger.MessageReadStatus

@Entity(tableName = MessageDbo.TABLE_NAME)
data class MessageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_CHAT_ID) val chatId: String,  // same as peerId (profileId)
    @ColumnInfo(name = COLUMN_CLIENT_ID) val clientId: String,
    @ColumnInfo(name = COLUMN_PEER_ID) val peerId: String,  // can be either profileId or [DomainUtil.CURRENT_USER_ID]
    @ColumnInfo(name = COLUMN_READ_STATUS) val readStatus: Int,
    @Deprecated("Unsupported")
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String = "",  // deprecated, left to avoid migration
    @ColumnInfo(name = COLUMN_TEXT) override val text: String,
    @ColumnInfo(name = COLUMN_TIMESTAMP) val ts: Long)
    : Mappable<Message>, IMessage {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_CHAT_ID = "chatId"
        const val COLUMN_CLIENT_ID = "clientId"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_READ_STATUS = "unread"  // Android 27 runs SQLite 3.19 that lacks RENAME COLUMN TO statement (since 3.25)
        const val COLUMN_SOURCE_FEED = "sourceFeed"  // deprecated, left to avoid migration
        const val COLUMN_TEXT = "text"
        const val COLUMN_TIMESTAMP = "ts"

        const val TABLE_NAME = "Messages"

        fun from(message: Message, readStatus: MessageReadStatus = message.readStatus): MessageDbo =
            MessageDbo(
                id = message.id,
                chatId = message.chatId,
                clientId = message.clientId,
                peerId = message.peerId,
                readStatus = readStatus.value,  // argument allows to override value from Message model
                text = message.text,
                ts = message.ts)
    }

    override fun map(): Message =
        Message(id = id, chatId = chatId, clientId = clientId, peerId = peerId,
                readStatus = MessageReadStatus.from(readStatus), text = text, ts = ts)
}
