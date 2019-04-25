package com.ringoid.base.manager.location

import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.utility.LOCATION_110m

object LocationUtils {

    fun onLocationChanged(location: GpsLocation, spm: ISharedPrefsManager, action: ((location: GpsLocation) -> Unit)? = null) {
        val prevLocation = spm.getLocation()
        if (prevLocation != null &&
            Math.abs(prevLocation.latitude - location.latitude) < LOCATION_110m &&
            Math.abs(prevLocation.longitude - location.longitude) < LOCATION_110m) {
            DebugLogUtil.v("Location has not changed")
        } else {
            DebugLogUtil.d("Location has changed enough")
            spm.saveLocation(location)
            action?.invoke(location)
        }
    }
}
