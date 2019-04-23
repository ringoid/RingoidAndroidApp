package com.ringoid.base.manager.location

import android.location.Location
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.utility.LOCATION_110m

object LocationUtils {

    fun onLocationChanged(location: Location, spm: ISharedPrefsManager, action: ((location: Location) -> Unit)? = null) {
        val prevLocation = spm.getLocation()
        if (prevLocation != null &&
            Math.abs(prevLocation.first - location.latitude) < LOCATION_110m &&
            Math.abs(prevLocation.second - location.longitude) < LOCATION_110m) {
            DebugLogUtil.v("Location has not changed")
        } else {
            DebugLogUtil.d("Location has changed enough")
            spm.saveLocation(location)
            action?.invoke(location)
        }
    }
}
