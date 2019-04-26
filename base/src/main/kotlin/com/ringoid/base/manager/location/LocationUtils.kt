package com.ringoid.base.manager.location

import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.utility.LOCATION_550m

object LocationUtils {

    fun onLocationChanged(location: GpsLocation, spm: ISharedPrefsManager, action: ((location: GpsLocation) -> Unit)? = null): Boolean {
        val prevLocation = spm.getLocation()  // if null - locate was never obtained before
        return if (prevLocation != null &&
            Math.abs(prevLocation.latitude - location.latitude) < LOCATION_550m &&
            Math.abs(prevLocation.longitude - location.longitude) < LOCATION_550m) {
            DebugLogUtil.v("Location has not changed")
            false
        } else {
            DebugLogUtil.d("Location has changed enough, update saved location")
            spm.saveLocation(location)  // update saved location
            action?.invoke(location)
            true
        }
    }
}
