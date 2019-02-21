package com.ringoid.origin.messenger

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.messenger.Message

data class ChatPayload(
    @Expose @SerializedName(COLUMN_POSITION) val position: Int = DomainUtil.BAD_POSITION,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_ID) val peerImageId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_URI) val peerImageUri: String? = null,
    @Expose @SerializedName(COLUMN_FIRST_USER_MESSAGE) var firstUserMessage: Message? = null) : IEssence, Parcelable {

    private constructor(source: Parcel): this(position = source.readInt(), peerId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageId = source.readString() ?: DomainUtil.BAD_ID, peerImageUri = source.readString(),
        firstUserMessage = source.readParcelable(Message::class.java.classLoader))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeInt(position)
            writeString(peerId)
            writeString(peerImageId)
            writeString(peerImageUri)
            writeParcelable(firstUserMessage, flags)
        }
    }

    companion object {
        const val COLUMN_POSITION = "position"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_PEER_IMAGE_ID = "peerImageId"
        const val COLUMN_PEER_IMAGE_URI = "peerImageUri"
        const val COLUMN_FIRST_USER_MESSAGE = "firstUserMessage"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ChatPayload> {
            override fun createFromParcel(source: Parcel): ChatPayload = ChatPayload(source)
            override fun newArray(size: Int): Array<ChatPayload?> = arrayOfNulls(size)
        }
    }
}
