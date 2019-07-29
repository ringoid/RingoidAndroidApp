package com.ringoid.domain.model.essence.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * "filter":{
 *   "minAge":18,
 *   "maxAge":33,
 *   "maxDistance":5000 // in meters
 * }
 */
data class FilterEssence(
    @Expose @SerializedName(COLUMN_MIN_AGE) val minAge: Int = 18,
    @Expose @SerializedName(COLUMN_MAX_AGE) val maxAge: Int = 80,
    @Expose @SerializedName(COLUMN_MAX_DISTANCE) val maxDistance: Int = 5000) : IEssence {

    companion object {
        const val COLUMN_MIN_AGE = "minAge"
        const val COLUMN_MAX_AGE = "maxAge"
        const val COLUMN_MAX_DISTANCE = "maxDistance"
    }

    override fun toSentryPayload(): String = "[minAge=$minAge, maxAge=$maxAge, maxDistance=$maxDistance]"
}
