package com.ringoid.domain.model.messenger

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.IListModel
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.goodHashCode
import com.ringoid.utility.randomString

/**
 * Message object: [id] corresponds to profileId of the opposite user,
 * same is true for [chatId]. On the other hand, [peerId] can be either
 * profileId of the opposite user, or [DomainUtil.CURRENT_USER_ID],
 * if current user (a customer) is the owner (actual sender) of this message.
 */
data class Message(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_CHAT_ID) val chatId: String,
    @Expose @SerializedName(COLUMN_CLIENT_ID) val clientId: String = id,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String,
    @Expose @SerializedName(COLUMN_READ_STATUS) var readStatus: MessageReadStatus,
    @Expose @SerializedName(COLUMN_TEXT) override val text: String,
    @Expose @SerializedName(COLUMN_TIMESTAMP) val ts: Long = 0L)
    : IEssence, IListModel, IMessage, Parcelable {

    private constructor(source: Parcel): this(
        id = source.readString() ?: DomainUtil.BAD_ID,
        chatId = source.readString() ?: DomainUtil.BAD_ID,
        clientId = source.readString() ?: DomainUtil.BAD_ID,
        peerId = source.readString() ?: DomainUtil.BAD_ID,
        readStatus = MessageReadStatus.from(source.readInt()),
        text = source.readString() ?: "",
        ts = source.readLong())

    override fun getModelId(): Long = id.goodHashCode()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with (dest) {
            writeString(id)
            writeString(chatId)
            writeString(clientId)
            writeString(peerId)
            writeInt(readStatus.value)
            writeString(text)
            writeLong(ts)
        }
    }

    fun isLocal(): Boolean = id == clientId
    fun isPeerMessage(): Boolean = peerId != DomainUtil.CURRENT_USER_ID
    fun isUserMessage(): Boolean = peerId == DomainUtil.CURRENT_USER_ID

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_CHAT_ID = "chatId"
        const val COLUMN_CLIENT_ID = "clientId"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_READ_STATUS = "readStatus"
        const val COLUMN_TEXT = "text"
        const val COLUMN_TIMESTAMP = "ts"

        @JvmField
        val CREATOR = object : Parcelable.Creator<Message> {
            override fun createFromParcel(source: Parcel): Message = Message(source)
            override fun newArray(size: Int): Array<Message?>  = arrayOfNulls(size)
        }
    }

    @DebugOnly
    fun toDebugString(): String = "Message: id=$[$id]($clientId), chatId=[$chatId], peer=[$peerId], rs=$readStatus"
}

val EmptyMessage = Message(id = randomString(), chatId = DomainUtil.BAD_ID, clientId = DomainUtil.BAD_ID,
                           peerId = DomainUtil.BAD_ID, readStatus = MessageReadStatus.ReadByUser, text = "", ts = 0L)

fun peerMessage(chatId: String): Message =
    Message(id = randomString(), chatId = chatId, clientId = DomainUtil.BAD_ID,
            peerId = chatId, readStatus = MessageReadStatus.ReadByUser, text = "", ts = 0L)

fun userMessage(chatId: String): Message =
    Message(id = randomString(), chatId = chatId, clientId = DomainUtil.BAD_ID,
            peerId = DomainUtil.CURRENT_USER_ID, readStatus = MessageReadStatus.ReadByPeer,
            text = "", ts = 0L)
