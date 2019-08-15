package com.ringoid.base.manager.location

class LocationServiceUnavailableException(provider: String, status: Int = -999) : RuntimeException("Location Service is unavailable: $provider, status: $status")
