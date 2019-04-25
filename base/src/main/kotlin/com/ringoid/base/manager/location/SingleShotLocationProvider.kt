package com.ringoid.base.manager.location

import android.content.Context
import android.location.*
import android.os.Bundle
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.misc.GpsLocation
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingleShotLocationProvider @Inject constructor(private val context: Context) : ILocationProvider {

    @SuppressWarnings("MissingPermission")
    override fun getLocation(precision: LocationPrecision): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationProviderForPrecision(precision)
                    .let { locationManager.getLastKnownLocation(it) }
                    ?.let { Single.just(GpsLocation.from(it)) }
                    ?: requestLocation(precision)  // no last location found in cache - request for location
            } ?: Single.error(NullPointerException("No location service available"))

    /**
     * Requests for single location update. Make sure permissions are granted and geoIP / GPS
     * services are enabled, depending on [precision].
     */
    @SuppressWarnings("MissingPermission")
    override fun requestLocation(precision: LocationPrecision): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                when (precision) {
                    LocationPrecision.COARSE ->
                        locationManager
                            .takeIf { it.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }
                            ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_COARSE } }
                    // --------------------------
                    LocationPrecision.FINE -> {
                        locationManager
                            .takeIf { it.isProviderEnabled(LocationManager.GPS_PROVIDER) }
                            ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_FINE } }
                    }
                }
                ?.let { criteria ->
                    Single.create<GpsLocation> { emitter ->
                        val listener = object : LocationListener {
                            override fun onLocationChanged(location: Location) {
                                emitter.onSuccess(GpsLocation.from(location))
                            }

                            override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                                if (status != LocationProvider.AVAILABLE) {
                                    emitter.onError(LocationServiceUnavailableException(provider, status))
                                }
                            }

                            override fun onProviderDisabled(provider: String) {
                                DebugLogUtil.v("Location service has been disabled by user")
                                emitter.onError(LocationServiceUnavailableException(provider))
                            }

                            override fun onProviderEnabled(provider: String) {
                                DebugLogUtil.v("Location service has been enabled by user")
                            }
                        }
                        emitter.setCancellable { locationManager.removeUpdates(listener) }
                        locationManager.requestSingleUpdate(criteria, listener, null)
                    }
                } ?: run { Single.error<GpsLocation>(LocationServiceUnavailableException(getLocationProviderForPrecision(precision))) }
            } ?: Single.error(NullPointerException("No location service available"))

    // --------------------------------------------------------------------------------------------
    private fun getLocationProviderForPrecision(precision: LocationPrecision): String =
        when (precision) {
            LocationPrecision.COARSE -> LocationManager.NETWORK_PROVIDER
            LocationPrecision.FINE -> LocationManager.GPS_PROVIDER
        }
}
