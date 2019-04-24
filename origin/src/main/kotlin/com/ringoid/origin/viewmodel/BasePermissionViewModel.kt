package com.ringoid.origin.viewmodel

import android.app.Application
import android.location.Location
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationPrecision
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.manager.location.LocationUtils
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.model.actions.LocationActionObject
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider

    protected open fun onLocationReceived() {}

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

        locationProvider
            .getLocation(LocationPrecision.COARSE)
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .subscribe(::onLocationChanged) {
                Timber.e(it)
                when (it) {
                    is LocationServiceUnavailableException -> viewState.value = ViewState.DONE(ASK_TO_ENABLE_LOCATION_SERVICE)
                }
            }
    }
}
