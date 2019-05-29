package com.ringoid.origin.messenger

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.view.main.LmmNavTab

data class ChatPayload(
    @Expose @SerializedName(COLUMN_POSITION) val position: Int = DomainUtil.BAD_POSITION,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_ID) val peerImageId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_URI) val peerImageUri: String? = null,
    @Expose @SerializedName(COLUMN_PEER_THUMB_URI) val peerThumbnailUri: String? = null,
    @Expose @SerializedName(COLUMN_FIRST_USER_MESSAGE) var firstUserMessage: Message? = null,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: LmmNavTab = LmmNavTab.MESSAGES)
    : IEssence, Parcelable {

    private constructor(source: Parcel): this(position = source.readInt(), peerId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageUri = source.readString(), peerThumbnailUri = source.readString(),
        sourceFeed = source.readSerializable() as? LmmNavTab ?: LmmNavTab.MESSAGES,
        firstUserMessage = source.readParcelable(Message::class.java.classLoader))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeInt(position)
            writeString(peerId)
            writeString(peerImageId)
            writeString(peerImageUri)
            writeString(peerThumbnailUri)
            writeSerializable(sourceFeed)
            writeParcelable(firstUserMessage, flags)
        }
    }

    companion object {
        const val COLUMN_POSITION = "position"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_PEER_IMAGE_ID = "peerImageId"
        const val COLUMN_PEER_IMAGE_URI = "peerImageUri"
        const val COLUMN_PEER_THUMB_URI = "peerThumbUri"
        const val COLUMN_FIRST_USER_MESSAGE = "firstUserMessage"
        const val COLUMN_SOURCE_FEED = "sourceFeed"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ChatPayload> {
            override fun createFromParcel(source: Parcel): ChatPayload = ChatPayload(source)
            override fun newArray(size: Int): Array<ChatPayload?> = arrayOfNulls(size)
        }
    }
}
