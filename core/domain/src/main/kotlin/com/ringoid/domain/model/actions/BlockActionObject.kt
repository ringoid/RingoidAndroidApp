package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

class BlockActionObject(
    @Expose @SerializedName(COLUMN_NUMBER_BLOCK_REASON) val numberOfBlockReason: Int,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_BLOCK,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_NUMBER_BLOCK_REASON = "blockReasonNum"

        @JvmField
        val CREATOR = object : Parcelable.Creator<BlockActionObject> {
            override fun createFromParcel(source: Parcel): BlockActionObject = BlockActionObject(source)
            override fun newArray(size: Int): Array<BlockActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "blockReason=$numberOfBlockReason"

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(numberOfBlockReason)
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong()
            .also { source.readString()  /** actionType ignored but read */ },
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        numberOfBlockReason = source.readInt())
}
