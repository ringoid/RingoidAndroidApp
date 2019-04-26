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
    override fun getLocation(): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationProvider(locationManager)
                    .also { DebugLogUtil.v("Location: using provider '$it'") }
                    ?.let { provider ->
                        locationManager.getLastKnownLocation(provider)
                            ?.also { DebugLogUtil.v("Location: last known location for provider '$provider' is: $it") }
                            ?.let { Single.just(GpsLocation.from(it)) }
                            ?: requestLocation(provider)  // no last location found in cache - request for location
                    }
                    ?: Single.error(LocationServiceUnavailableException("any", status = -4))
            } ?: Single.error(NullPointerException("No location service available"))

    @SuppressWarnings("MissingPermission")
    override fun getLocation(precision: LocationPrecision): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationProviderForPrecision(precision)
                    .also { DebugLogUtil.v("Location: using provider '$it' for precision '$precision'") }
                    .let { locationManager.getLastKnownLocation(it) }
                    ?.also { DebugLogUtil.v("Location: last known location for precision '$precision' is: $it") }
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
                getLocationCriteriaByPrecision(locationManager, precision)
                    ?.also { DebugLogUtil.v("Location: request for location for precision '$precision' and criteria: $it") }
                    ?.let { criteria -> requestLocationWithManagerAndCriteria(locationManager, criteria) }
                    ?: run { Single.error<GpsLocation>(LocationServiceUnavailableException(getLocationProviderForPrecision(precision), status = -3)) }
            } ?: Single.error(NullPointerException("No location service available"))

    // ------------------------------------------
    private fun requestLocation(provider: String): Single<GpsLocation> =
        (context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.let { locationManager ->
                getLocationCriteriaByProvider(provider)
                    ?.also { DebugLogUtil.v("Location: request for location with provider '$provider' and criteria: $it") }
                    ?.let { criteria -> requestLocationWithManagerAndCriteria(locationManager, criteria) }
                    ?: run { Single.error<GpsLocation>(LocationServiceUnavailableException(provider, status = -2)) }
            } ?: Single.error(NullPointerException("No location service available"))

    // --------------------------------------------------------------------------------------------
    private fun getLocationCriteriaByPrecision(locationManager: LocationManager, precision: LocationPrecision): Criteria? =
        when (precision) {
            LocationPrecision.ANY ->
                locationManager
                    .takeIf { it.isProviderEnabled("fused") }
                    ?.let { Criteria().apply { accuracy = Criteria.ACCURACY_COARSE } }
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

    private fun getLocationCriteriaByProvider(provider: String): Criteria? =
        when (provider) {
            "fused",
            LocationManager.NETWORK_PROVIDER -> Criteria().apply { accuracy = Criteria.ACCURACY_COARSE }
            LocationManager.GPS_PROVIDER -> Criteria().apply { accuracy = Criteria.ACCURACY_FINE }
            else -> null
        }

    private fun getLocationProvider(locationManager: LocationManager): String? =
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) LocationManager.NETWORK_PROVIDER
        else if (locationManager.isProviderEnabled("fused")) "fused"
        else null

    private fun getLocationProviderForPrecision(precision: LocationPrecision): String =
        when (precision) {
            LocationPrecision.ANY -> "fused"
            LocationPrecision.COARSE -> LocationManager.NETWORK_PROVIDER
            LocationPrecision.FINE -> LocationManager.GPS_PROVIDER
        }

    @SuppressWarnings("MissingPermission")
    private fun requestLocationWithManagerAndCriteria(locationManager: LocationManager, criteria: Criteria): Single<GpsLocation> =
        Single.create<GpsLocation> { emitter ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    DebugLogUtil.v("Location: obtained (${location.latitude}, ${location.longitude})")
                    emitter.onSuccess(GpsLocation.from(location))
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                    DebugLogUtil.v("Location: status has changed to $status for provider '$provider': $extras".trim())
                    if (status != LocationProvider.AVAILABLE) {
                        emitter.onError(LocationServiceUnavailableException(provider, status))
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    DebugLogUtil.v("Location service has been disabled by user, provider: '$provider'")
                    emitter.onError(LocationServiceUnavailableException(provider, status = -1))
                }

                override fun onProviderEnabled(provider: String) {
                    DebugLogUtil.v("Location service has been enabled by user, provider: '$provider'")
                }
            }
            emitter.setCancellable { locationManager.removeUpdates(listener) }
            locationManager.requestSingleUpdate(criteria, listener, null)
        }
}
