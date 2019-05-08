package com.ringoid.origin.viewmodel

import android.app.Application
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
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
        Timber.v("onLocationPermissionGranted($handleCode)")
        onLocationPermissionGrantedAction(handleCode)

        locationProvider.location()
            .subscribe({ onLocationReceived(handleCode) }) {
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
