package com.ringoid.origin.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.LocationServiceUnavailableException
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionViewModel(app: Application) : BaseViewModel(app) {

    @Inject lateinit var locationProvider: ILocationProvider
    private val askToEnableLocationServiceOneShot by lazy { MutableLiveData<OneShot<Int>>() }
    fun askToEnableLocationServiceOneShot(): LiveData<OneShot<Int>> = askToEnableLocationServiceOneShot

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
                DebugLogUtil.d("Location [client]: start get at $start")
            }
            .doOnSuccess { DebugLogUtil.d("Location [client]: success [$it]") }
            .doOnError { SentryUtil.capture(it, "Location get failed") }
            .doFinally {
                val elapsed = System.currentTimeMillis() - start
                DebugLogUtil.d("Location [client]: obtain location has taken $elapsed ms")
            }
            .autoDisposable(this)
            .subscribe({ onLocationReceived(handleCode) }) {
                when (it) {
                    is LocationServiceUnavailableException ->
                        askToEnableLocationServiceOneShot.value = OneShot(handleCode)
                }
            }
    }

    internal fun onLocationPermissionDenied(handleCode: Int) {
        Timber.v("onLocationPermissionDenied($handleCode)")
        onLocationPermissionDeniedAction(handleCode)
    }
}
