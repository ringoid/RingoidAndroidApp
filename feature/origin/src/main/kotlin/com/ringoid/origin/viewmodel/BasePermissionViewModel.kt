package com.ringoid.origin.viewmodel

import android.app.Application
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.origin.view.base.ASK_TO_ENABLE_LOCATION_SERVICE
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider

    protected open fun onLocationPermissionGrantedAction(handleCode: Int) {}
    protected open fun onLocationPermissionDeniedAction(handleCode: Int) {}
    protected open fun onLocationReceived(handleCode: Int) {}

    /* Permission */
    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    internal fun onLocationPermissionGranted(handleCode: Int) {
        Timber.v("onLocationPermissionGranted($handleCode)")
        onLocationPermissionGrantedAction(handleCode)

        var start = 0L
        locationProvider.location()
            .doOnSubscribe {
                start = System.currentTimeMillis()
                DebugLogUtil.d("Location: start get at $start")
            }
            .doOnSuccess { DebugLogUtil.d("Location: success [$it]") }
            .doOnError {
                DebugLogUtil.e(it, "Location: failed")
                SentryUtil.capture(it, "Location get failed")
            }
            .doFinally {
                val elapsed = System.currentTimeMillis() - start
                DebugLogUtil.d("Location: obtain location has taken $elapsed ms")
            }
            .autoDisposable(this)
            .subscribe({ onLocationReceived(handleCode) }) {
                when (it) {
                    is LocationServiceUnavailableException -> viewState.value = ViewState.DONE(ASK_TO_ENABLE_LOCATION_SERVICE(handleCode))
                }
            }
    }

    internal fun onLocationPermissionDenied(handleCode: Int) {
        Timber.v("onLocationPermissionDenied($handleCode)")
        onLocationPermissionDeniedAction(handleCode)
    }
}
