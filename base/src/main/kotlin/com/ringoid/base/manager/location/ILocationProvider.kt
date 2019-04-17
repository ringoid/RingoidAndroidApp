package com.ringoid.base.manager.location

import android.location.Location
import io.reactivex.Single

interface ILocationProvider {

    fun getLocation(precision: LocationPrecision): Single<Location>
    fun requestLocation(precision: LocationPrecision): Single<Location>
}
