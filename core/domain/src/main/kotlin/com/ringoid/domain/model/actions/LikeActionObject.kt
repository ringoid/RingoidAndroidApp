package com.ringoid.domain.model.actions

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.action_storage.Immediate
import com.ringoid.utility.randomInt

class LikeActionObject(
    @Expose @SerializedName(COLUMN_LIKE_COUNT) val likeCount: Int = 1,
    id: Int = randomInt(),
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String,
    targetImageId: String, targetUserId: String)
    : ActionObject(
        id = id,
        actionTime = actionTime, actionType = ACTION_TYPE_LIKE,
        sourceFeed = sourceFeed,
        targetImageId = targetImageId, targetUserId = targetUserId,
        triggerStrategies = listOf(Immediate)) {

    companion object {
        const val COLUMN_LIKE_COUNT = "likeCount"

        @JvmField
        val CREATOR = object : Parcelable.Creator<LikeActionObject> {
            override fun createFromParcel(source: Parcel): LikeActionObject = LikeActionObject(source)
            override fun newArray(size: Int): Array<LikeActionObject?> = arrayOfNulls(size)
        }
    }

    override fun propertyString(): String? = "likeCount=$likeCount"

    // --------------------------------------------------------------------------------------------
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(likeCount)
    }

    private constructor(source: Parcel): this(
        id = source.readInt(),
        actionTime = source.readLong()
            .also { source.readString()  /** actionType ignored but read */ },
        sourceFeed = source.readString(),
        targetImageId = source.readString(),
        targetUserId = source.readString(),
        likeCount = source.readInt())
}
