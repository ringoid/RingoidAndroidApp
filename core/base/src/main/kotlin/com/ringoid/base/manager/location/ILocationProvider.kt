package com.ringoid.base.manager.location

import com.ringoid.domain.misc.GpsLocation
import io.reactivex.Single

interface ILocationProvider {

    fun location(): Single<GpsLocation>
    fun getLocation(): Single<GpsLocation>
    fun getLocation(precision: LocationPrecision): Single<GpsLocation>
    fun requestLocation(precision: LocationPrecision): Single<GpsLocation>
}
