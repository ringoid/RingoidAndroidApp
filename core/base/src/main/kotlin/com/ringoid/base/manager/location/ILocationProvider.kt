package com.ringoid.base.manager.location

import com.ringoid.domain.misc.GpsLocation
import io.reactivex.Single

interface ILocationProvider {

    fun location(): Single<GpsLocation>
}
