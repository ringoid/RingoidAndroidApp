package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.DelayFromLast
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.action_storage.VIEW_DELAY_ON_TRIGGER
import com.ringoid.utility.randomInt

class ViewChatActionObject(
    @Expose @SerializedName(COLUMN_VIEW_CHAT_TIME_MILLIS) override var timeInMillis: Long = 1L,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = listOf(DelayFromLast(VIEW_DELAY_ON_TRIGGER)))
    : DurableActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_VIEW_CHAT,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = triggerStrategies), IDurableAction {

    companion object {
        const val COLUMN_VIEW_CHAT_TIME_MILLIS = "viewTimeMillis"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ViewChatActionObject> {
            override fun createFromParcel(source: Parcel): ViewChatActionObject = ViewChatActionObject(source)
            override fun newArray(size: Int): Array<ViewChatActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "timeInMillis=$timeInMillis"

    override fun toActionString(): String = "$actionType($timeInMillis ms,${targetIdsStr()}${if (isHidden) ",hidden" else ""})"

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(timeInMillis)
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong()
            .also { source.readString()  /** actionType ignored but read */ },
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        timeInMillis = source.readLong())
}
