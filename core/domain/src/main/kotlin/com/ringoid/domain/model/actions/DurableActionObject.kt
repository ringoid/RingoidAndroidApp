package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.utility.randomInt

open class DurableActionObject(
    override var timeInMillis: Long = 1L,
    override var isHidden: Boolean = false,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(), actionType: String,
    sourceFeed: String,
    targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = emptyList())
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = actionType,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = triggerStrategies), IDurableAction {

    fun advance(): DurableActionObject {
        timeInMillis = System.currentTimeMillis() - actionTime
        return this
    }

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        with (dest) {
            writeLong(timeInMillis)
            writeInt(if (isHidden) 1 else 0)
        }
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong(),
        actionType = source.readString(),
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        timeInMillis = source.readLong(),
        isHidden = source.readInt() != 0)

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DurableActionObject> {
            override fun createFromParcel(source: Parcel): DurableActionObject = DurableActionObject(source)
            override fun newArray(size: Int): Array<DurableActionObject?> = arrayOfNulls(size)
        }
    }
}
