package com.ringoid.base.manager.location

import com.ringoid.domain.misc.GpsLocation
import com.ringoid.utility.LOCATION_550m
import kotlin.math.abs

object LocationUtils {

    const val LocationManager_FUSED_PROVIDER = "fused"

    fun diffLocation(oldLocation: GpsLocation?, newLocation: GpsLocation): Boolean =
        oldLocation == null ||
            (abs(oldLocation.latitude - newLocation.latitude) >= LOCATION_550m ||
             abs(oldLocation.longitude - newLocation.longitude) >= LOCATION_550m)
}
