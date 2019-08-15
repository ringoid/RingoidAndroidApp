package com.ringoid.domain.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.IEssence

data class Filters internal constructor(
    @Expose @SerializedName(COLUMN_MIN_AGE) val minAge: Int = DomainUtil.FILTER_MIN_AGE,
    @Expose @SerializedName(COLUMN_MAX_AGE) val maxAge: Int = DomainUtil.FILTER_MAX_AGE,
    @Expose @SerializedName(COLUMN_MAX_DISTANCE) val maxDistance: Int = DomainUtil.FILTER_MAX_DISTANCE) : IEssence {

    companion object {
        const val COLUMN_MIN_AGE = "minAge"
        const val COLUMN_MAX_AGE = "maxAge"
        const val COLUMN_MAX_DISTANCE = "maxDistance"

        fun create(minAge: Int = DomainUtil.FILTER_MIN_AGE,
                   maxAge: Int = DomainUtil.FILTER_MAX_AGE,
                   maxDistance: Int = DomainUtil.FILTER_MAX_DISTANCE): Filters =
            Filters(
                minAge = minOf(maxOf(minAge, DomainUtil.FILTER_MIN_AGE), DomainUtil.FILTER_MAX_AGE),
                maxAge = minOf(maxOf(maxAge, DomainUtil.FILTER_MIN_AGE), DomainUtil.FILTER_MAX_AGE),
                maxDistance = minOf(maxOf(maxDistance, DomainUtil.FILTER_MIN_DISTANCE), DomainUtil.FILTER_MAX_DISTANCE))

        fun create(other: Filters): Filters =
            create(minAge = other.minAge, maxAge = other.maxAge, maxDistance = other.maxDistance)

        fun createWithAgeRange(minAge: Int = DomainUtil.FILTER_MIN_AGE,
                               maxAge: Int = DomainUtil.FILTER_MAX_AGE,
                               maxDistance: Int = DomainUtil.FILTER_MAX_DISTANCE,
                               ageRange: Int = DomainUtil.FILTER_AGE_MIN_RANGE): Filters {
            val filters = create(minAge = minAge, maxAge = maxAge, maxDistance = maxDistance)
            val ageDiff = filters.maxAge - filters.minAge
            return if (ageDiff < ageRange) create(minAge = minAge, maxAge = maxAge + ageRange - ageDiff, maxDistance = maxDistance)
                   else filters
        }

        fun createWithAgeRange(other: Filters, ageRange: Int = DomainUtil.FILTER_AGE_MIN_RANGE): Filters =
            createWithAgeRange(minAge = other.minAge, maxAge = other.maxAge, maxDistance = other.maxDistance, ageRange = ageRange)
    }
}

val NoFilters = Filters(minAge = DomainUtil.FILTER_MIN_AGE,
                        maxAge = DomainUtil.FILTER_MAX_AGE,
                        maxDistance = DomainUtil.FILTER_MAX_DISTANCE)

val EmptyFilters = Filters(minAge = DomainUtil.FILTER_MAX_AGE,
                           maxAge = DomainUtil.FILTER_MAX_AGE,
                           maxDistance = DomainUtil.FILTER_MAX_DISTANCE)
