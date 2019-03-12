package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.action_storage.DelayFromLast
import com.ringoid.domain.action_storage.TriggerStrategy
import com.ringoid.domain.action_storage.VIEW_DELAY_ON_TRIGGER

class ViewActionObject(
    @Expose @SerializedName(COLUMN_VIEW_COUNT) val count: Int = 1,
    @Expose @SerializedName(COLUMN_VIEW_TIME_MILLIS) var timeInMillis: Long = 1L,
    actionTime: Long = System.currentTimeMillis(),
    sourceFeed: String, targetImageId: String, targetUserId: String,
    triggerStrategies: List<TriggerStrategy> = listOf(DelayFromLast(VIEW_DELAY_ON_TRIGGER)))
    : ActionObject(actionTime = actionTime, actionType = ACTION_TYPE_VIEW, sourceFeed = sourceFeed,
                   targetImageId = targetImageId, targetUserId = targetUserId,
                   triggerStrategies = triggerStrategies) {

    fun advance(): ViewActionObject {
        timeInMillis = System.currentTimeMillis() - actionTime
        return this
    }

    /**
     * Clones this object, but drops it's accumulated lifetime.
     */
    fun recreated(): ViewActionObject =
        ViewActionObject(timeInMillis = 0L, sourceFeed = sourceFeed,
                         targetImageId = targetImageId, targetUserId = targetUserId)

    companion object {
        const val COLUMN_VIEW_COUNT = "viewCount"
        const val COLUMN_VIEW_TIME_MILLIS = "viewTimeMillis"
    }

    override fun propertyString(): String? = "count=$count, timeInMillis=$timeInMillis"

    override fun toActionString(): String = "VIEW($timeInMillis,${targetIdsStr()})"
}
