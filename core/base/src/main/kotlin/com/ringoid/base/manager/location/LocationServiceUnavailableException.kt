package com.ringoid.base.manager.location

class LocationServiceUnavailableException(provider: String, val status: Int = STATUS_UNKNOWN)
    : RuntimeException("Location Service is unavailable: $provider, status: $status") {

    companion object {
        const val STATUS_SERVICE_DISABLED_BY_USER = -1
        const val STATUS_NO_CRITERIA_BY_PROVIDER = -2
        const val STATUS_NO_CRITERIA_BY_PRECISION = -3
        const val STATUS_SERVICE_TURNED_OFF = -4
        const val STATUS_UNKNOWN = -999
    }
}
