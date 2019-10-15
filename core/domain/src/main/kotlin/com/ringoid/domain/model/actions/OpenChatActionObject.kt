package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.DelayFromLast
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.action_storage.VIEW_DELAY_ON_TRIGGER
import com.ringoid.utility.randomInt

@Deprecated("Use [ViewChatActionObject] instead")
class OpenChatActionObject(
    @Expose @SerializedName(COLUMN_OPEN_CHAT_COUNT) val count: Int = 1,
    @Expose @SerializedName(COLUMN_OPEN_CHAT_TIME_MILLIS) override var timeInMillis: Long = 1L,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = listOf(DelayFromLast(VIEW_DELAY_ON_TRIGGER)))
    : DurableActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_OPEN_CHAT,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = triggerStrategies), IDurableAction {

    companion object {
        const val COLUMN_OPEN_CHAT_COUNT = "openChatCount"
        const val COLUMN_OPEN_CHAT_TIME_MILLIS = "openChatTimeMillis"

        @JvmField
        val CREATOR = object : Parcelable.Creator<OpenChatActionObject> {
            override fun createFromParcel(source: Parcel): OpenChatActionObject = OpenChatActionObject(source)
            override fun newArray(size: Int): Array<OpenChatActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "count=$count, timeInMillis=$timeInMillis"

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        with (dest) {
            writeInt(count)
            writeLong(timeInMillis)
        }
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong()
            .also { source.readString()  /** actionType ignored but read */ },
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        count = source.readInt(),
        timeInMillis = source.readLong())
}
