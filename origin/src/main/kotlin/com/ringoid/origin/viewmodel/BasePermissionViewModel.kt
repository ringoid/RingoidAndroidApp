package com.ringoid.origin.viewmodel

import android.app.Application
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.manager.location.LocationUtils
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.model.actions.LocationActionObject
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider

    protected open fun onLocationPermissionGrantedAction(handleCode: Int) {}
    protected open fun onLocationPermissionDeniedAction(handleCode: Int) {}
    protected open fun onLocationReceived(handleCode: Int) {}

    /* Permission */
    // --------------------------------------------------------------------------------------------
    fun onLocationPermissionGranted(handleCode: Int) {
        fun onLocationChanged(location: GpsLocation) {
            LocationUtils.onLocationChanged(location, spm) {
                val aobj = LocationActionObject(location.latitude, location.longitude)
                actionObjectPool.put(aobj)
            }
            onLocationReceived(handleCode)
        }

        Timber.v("onLocationPermissionGranted($handleCode)")
        onLocationPermissionGrantedAction(handleCode)

        locationProvider
            .getLocation()
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
            .subscribe(::onLocationChanged) {
                DebugLogUtil.e(it)
                when (it) {
                    is LocationServiceUnavailableException -> viewState.value = ViewState.DONE(ASK_TO_ENABLE_LOCATION_SERVICE(handleCode))
                }
            }
    }

    fun onLocationPermissionDenied(handleCode: Int) {
        Timber.v("onLocationPermissionDenied($handleCode)")
        onLocationPermissionDeniedAction(handleCode)
    }
}
