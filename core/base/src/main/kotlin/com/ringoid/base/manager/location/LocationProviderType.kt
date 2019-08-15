package com.ringoid.base.manager.location

import android.location.LocationManager

enum class LocationProviderType(val provider: String) {
    NETWORK(LocationManager.NETWORK_PROVIDER),  // use coarse, but fast provider first
    GPS(LocationManager.GPS_PROVIDER),
    FUSED(LocationUtils.LocationManager_FUSED_PROVIDER);  // order matters

    companion object {
        val values: Array<LocationProviderType> = LocationProviderType.values()
    }

    fun isEnabled(locationManager: LocationManager): Boolean =
        locationManager.isProviderEnabled(provider)
}
