package com.ringoid.base.viewmodel.advanced

import android.app.Application
import android.location.Location
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationPrecision
import com.ringoid.base.manager.location.LocationUtils
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.actions.LocationActionObject
import timber.log.Timber
import javax.inject.Inject

abstract class BaseLocationViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider

    protected abstract fun onLocationReceived()

    /* Permission */
    // --------------------------------------------------------------------------------------------
    fun onLocationPermissionGranted() {
        fun onLocationChanged(location: Location) {
            LocationUtils.onLocationChanged(location, spm) {
                val aobj = LocationActionObject(location.latitude, location.longitude)
                actionObjectPool.put(aobj)
            }
            onLocationReceived()
        }

        locationProvider.getLocation(LocationPrecision.COARSE)
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .subscribe(::onLocationChanged, Timber::e)
    }
}
