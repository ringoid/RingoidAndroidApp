package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

class MessageActionObject(
    @Expose @SerializedName(COLUMN_CLIENT_ID) val clientId: String,
    @Expose @SerializedName(COLUMN_TEXT) val text: String,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_MESSAGE,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_CLIENT_ID = "clientMsgId"
        const val COLUMN_TEXT = "text"

        @JvmField
        val CREATOR = object : Parcelable.Creator<MessageActionObject> {
            override fun createFromParcel(source: Parcel): MessageActionObject = MessageActionObject(source)
            override fun newArray(size: Int): Array<MessageActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "clientId=${clientId.substring(0..3)},text=$text"

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        with (dest) {
            writeString(clientId)
            writeString(text)
        }
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong()
            .also { source.readString()  /** actionType ignored but read */ },
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        clientId = source.readString(),
        text = source.readString())
}
