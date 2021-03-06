package com.ringoid.domain.model.actions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.actions.ActionObject.Companion.ACTION_TYPE_LOCATION
import com.ringoid.utility.ValueUtils
import com.ringoid.utility.randomInt

class LocationActionObject(
    @Expose @SerializedName(COLUMN_LATITUDE) val latitude: Double,
    @Expose @SerializedName(COLUMN_LONGITUDE) val longitude: Double,
    id: Int = randomInt(), actionTime: Long = System.currentTimeMillis())
    : OriginActionObject(id = id, actionTime = actionTime, actionType = ACTION_TYPE_LOCATION) {

    override fun isValid(): Boolean = ValueUtils.isValidLocation(latitude = latitude, longitude = longitude)

    companion object {
        const val COLUMN_LATITUDE = "lat"
        const val COLUMN_LONGITUDE = "lon"
    }

    override fun propertyString(): String? = "lat=$latitude,lon=$longitude"
}
