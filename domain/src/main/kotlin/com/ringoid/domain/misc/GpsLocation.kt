package com.ringoid.domain.misc

import android.location.Location

data class GpsLocation(val latitude: Double, val longitude: Double) {

    companion object {
        fun from(location: Location): GpsLocation =
            GpsLocation(latitude = location.latitude, longitude = location.longitude)
    }
}
