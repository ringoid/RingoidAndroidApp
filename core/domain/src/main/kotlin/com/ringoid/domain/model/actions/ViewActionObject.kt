package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.*
import com.ringoid.utility.randomInt

class ViewActionObject(
    @Expose @SerializedName(COLUMN_VIEW_COUNT) val count: Int = 1,
    @Expose @SerializedName(COLUMN_VIEW_TIME_MILLIS) override var timeInMillis: Long = 1L,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = listOf(CountFromLast(COUNT_ON_TRIGGER), DelayFromLast(VIEW_DELAY_ON_TRIGGER)))
    : DurableActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_VIEW,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = triggerStrategies) {

    fun copyWith(timeInMillis: Long, sourceFeed: String): ViewActionObject =
        ViewActionObject(timeInMillis = timeInMillis, sourceFeed = sourceFeed,
                         targetImageId = targetImageId, targetUserId = targetUserId)

    /**
     * Clones this object, but drops it's accumulated lifetime.
     */
    fun recreated(): ViewActionObject =
        ViewActionObject(timeInMillis = 0L, sourceFeed = sourceFeed,
                         targetImageId = targetImageId, targetUserId = targetUserId)

    companion object {
        const val COLUMN_VIEW_COUNT = "viewCount"
        const val COLUMN_VIEW_TIME_MILLIS = "viewTimeMillis"

        @JvmField
        val CREATOR = object : Parcelable.Creator<ViewActionObject> {
            override fun createFromParcel(source: Parcel): ViewActionObject = ViewActionObject(source)
            override fun newArray(size: Int): Array<ViewActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "count=$count, timeInMillis=$timeInMillis"

    override fun toActionString(): String = "$actionType($timeInMillis ms,${targetIdsStr()}${if (isHidden) ",hidden" else ""})"

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
