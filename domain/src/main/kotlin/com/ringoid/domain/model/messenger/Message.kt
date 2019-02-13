package com.ringoid.domain.model.messenger

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IListModel
import com.ringoid.utility.randomString

data class Message(val id: String, val chatId: String, val peerId: String, val text: String)
    : IListModel, Parcelable {

    private constructor(source: Parcel): this(id = source.readString() ?: DomainUtil.BAD_ID,
        chatId = source.readString() ?: DomainUtil.BAD_ID, peerId = source.readString() ?: DomainUtil.BAD_ID,
        text = source.readString() ?: "")

    override fun getModelId(): Long = id.hashCode().toLong()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(chatId)
            writeString(peerId)
            writeString(text)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Message> {
            override fun createFromParcel(source: Parcel): Message = Message(source)
            override fun newArray(size: Int): Array<Message?>  = arrayOfNulls(size)
        }
    }
}

val EmptyMessage = Message(id = randomString(), chatId = DomainUtil.BAD_ID, peerId = DomainUtil.BAD_ID, text = "")
