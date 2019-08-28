package com.ringoid.origin.messenger.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence
import com.ringoid.origin.model.OnlineStatus
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.origin.view.main.LmmNavTab

data class ChatPayload(
    @Expose @SerializedName(COLUMN_COUNT_PEER_MESSAGES) var peerMessagesCount: Int = 0,
    @Expose @SerializedName(COLUMN_COUNT_USER_MESSAGES) var userMessagesCount: Int = 0,
    @Expose @SerializedName(COLUMN_POSITION) val position: Int = DomainUtil.BAD_POSITION,
    @Expose @SerializedName(COLUMN_PEER_ID) val peerId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_ID) val peerImageId: String = DomainUtil.BAD_ID,
    @Expose @SerializedName(COLUMN_PEER_IMAGE_URI) val peerImageUri: String? = null,
    @Expose @SerializedName(COLUMN_PEER_THUMB_URI) val peerThumbnailUri: String? = null,
    @Deprecated("LMM -> LC") @Expose @SerializedName(COLUMN_FLAG_CHAT_EMPTY) var isChatEmpty: Boolean = true,  // TODO: remove field
    @Expose @SerializedName(COLUMN_ONLINE_STATUS) var onlineStatus: OnlineStatus? = null,
    @Expose @SerializedName(COLUMN_SOURCE_FEED) val sourceFeed: LcNavTab = LcNavTab.MESSAGES,
    @Deprecated("LMM -> LC") @Expose @SerializedName(COLUMN_SOURCE_FEED_COMPAT) val sourceFeedCompat: LmmNavTab = LmmNavTab.MESSAGES)  // TODO: remove field
    : IEssence, Parcelable {

    private constructor(source: Parcel): this(
        peerMessagesCount = source.readInt(),
        userMessagesCount = source.readInt(),
        position = source.readInt(),
        peerId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageId = source.readString() ?: DomainUtil.BAD_ID,
        peerImageUri = source.readString(),
        peerThumbnailUri = source.readString(),
        isChatEmpty = source.readInt() != 0,
        onlineStatus = source.readSerializable() as? OnlineStatus,
        sourceFeed = source.readSerializable() as? LcNavTab ?: LcNavTab.MESSAGES,
        sourceFeedCompat = source.readSerializable() as? LmmNavTab ?: LmmNavTab.MESSAGES)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with (dest) {
            writeInt(peerMessagesCount)
            writeInt(userMessagesCount)
            writeInt(position)
            writeString(peerId)
            writeString(peerImageId)
            writeString(peerImageUri)
            writeString(peerThumbnailUri)
            writeInt(if (isChatEmpty) 1 else 0)
            writeSerializable(onlineStatus)
            writeSerializable(sourceFeed)
            writeSerializable(sourceFeedCompat)
        }
    }

    companion object {
        const val COLUMN_COUNT_PEER_MESSAGES = "peerMessagesCount"
        const val COLUMN_COUNT_USER_MESSAGES = "userMessagesCount"
        const val COLUMN_POSITION = "position"
        const val COLUMN_PEER_ID = "peerId"
        const val COLUMN_PEER_IMAGE_ID = "peerImageId"
        const val COLUMN_PEER_IMAGE_URI = "peerImageUri"
        const val COLUMN_PEER_THUMB_URI = "peerThumbUri"
        const val COLUMN_FLAG_CHAT_EMPTY = "isChatEmpty"
        const val COLUMN_ONLINE_STATUS = "onlineStatus"
        const val COLUMN_SOURCE_FEED = "sourceFeed"
        const val COLUMN_SOURCE_FEED_COMPAT = "sourceFeedCompat"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ChatPayload> {
            override fun createFromParcel(source: Parcel): ChatPayload = ChatPayload(source)
            override fun newArray(size: Int): Array<ChatPayload?> = arrayOfNulls(size)
        }
    }
}
