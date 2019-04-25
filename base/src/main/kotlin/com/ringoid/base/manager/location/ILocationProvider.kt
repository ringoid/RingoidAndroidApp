package com.ringoid.base.manager.location

import com.ringoid.domain.misc.GpsLocation
import io.reactivex.Single

interface ILocationProvider {

    fun getLocation(precision: LocationPrecision): Single<GpsLocation>
    fun requestLocation(precision: LocationPrecision): Single<GpsLocation>
}
