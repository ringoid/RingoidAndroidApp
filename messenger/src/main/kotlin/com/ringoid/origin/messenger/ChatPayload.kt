package com.ringoid.origin.messenger

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID

data class ChatPayload(
    val position: Int = DomainUtil.BAD_POSITION,
    val peerId: String = DomainUtil.BAD_ID,
    val peerImageId: String = DomainUtil.BAD_ID) : Parcelable {

    private constructor(source: Parcel): this(position = source.readInt(), peerId = source.readString() ?: BAD_ID,
                                              peerImageId = source.readString() ?: BAD_ID)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeInt(position)
            writeString(peerId)
            writeString(peerImageId)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ChatPayload> {
            override fun createFromParcel(source: Parcel): ChatPayload =
                ChatPayload(source)
            override fun newArray(size: Int): Array<ChatPayload?> = arrayOfNulls(size)
        }
    }
}
