package com.ringoid.origin.messenger

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Message

data class ChatPayload(
    val position: Int = DomainUtil.BAD_POSITION,
    val peerId: String = DomainUtil.BAD_ID,
    val peerImageId: String = DomainUtil.BAD_ID,
    var firstUserMessage: Message? = null) : Parcelable {

    private constructor(source: Parcel): this(position = source.readInt(), peerId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageId = source.readString() ?: DomainUtil.BAD_ID, firstUserMessage = source.readParcelable(Message::class.java.classLoader))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeInt(position)
            writeString(peerId)
            writeString(peerImageId)
            writeParcelable(firstUserMessage, flags)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ChatPayload> {
            override fun createFromParcel(source: Parcel): ChatPayload = ChatPayload(source)
            override fun newArray(size: Int): Array<ChatPayload?> = arrayOfNulls(size)
        }
    }
}
