package com.ringoid.domain.model.essence.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence

/**
 * "filter":{
 *   "minAge":18,
 *   "maxAge":33,
 *   "maxDistance":5000 // in meters
 * }
 */
class FilterEssence private constructor(
    @Expose @SerializedName(COLUMN_MIN_AGE) val minAge: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_MAX_AGE) val maxAge: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_MAX_DISTANCE) val maxDistance: Int = DomainUtil.UNKNOWN_VALUE) : IEssence {

    companion object {
        const val COLUMN_MIN_AGE = "minAge"
        const val COLUMN_MAX_AGE = "maxAge"
        const val COLUMN_MAX_DISTANCE = "maxDistance"

        fun create(minAge: Int = DomainUtil.FILTER_MIN_AGE, maxAge: Int = DomainUtil.FILTER_MAX_AGE,
                   maxDistance: Int = DomainUtil.FILTER_MIN_DISTANCE): FilterEssence {
            val essence = FilterEssence(
                minAge = maxOf(minAge, DomainUtil.FILTER_MIN_AGE),
                maxAge = maxOf(maxAge, DomainUtil.FILTER_MIN_AGE),
                maxDistance = maxOf(maxDistance, DomainUtil.FILTER_MIN_DISTANCE))

            return FilterEssence(
                minAge = essence.minAge.takeIf { it < DomainUtil.FILTER_MAX_AGE } ?: DomainUtil.UNKNOWN_VALUE,
                maxAge = essence.maxAge.takeIf { it < DomainUtil.FILTER_MAX_AGE } ?: DomainUtil.UNKNOWN_VALUE,
                maxDistance = essence.maxDistance.takeIf { it < DomainUtil.FILTER_MAX_DISTANCE } ?: DomainUtil.UNKNOWN_VALUE )
        }
    }

    override fun toJson(): String =
        mutableListOf<String>().apply {
            minAge.takeIf { it != DomainUtil.UNKNOWN_VALUE }?.let { add("\"$COLUMN_MIN_AGE\":$minAge") }
            maxAge.takeIf { it != DomainUtil.UNKNOWN_VALUE }?.let { add("\"$COLUMN_MAX_AGE\":$maxAge") }
            maxDistance.takeIf { it != DomainUtil.UNKNOWN_VALUE }?.let { add("\"$COLUMN_MAX_DISTANCE\":$maxDistance") }
        }
        .joinToString(",", "{", "}")

    override fun toSentryPayload(): String = "[minAge=$minAge, maxAge=$maxAge, maxDistance=$maxDistance]"
}
