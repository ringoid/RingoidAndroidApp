package com.ringoid.base.manager.location

class LocationServiceUnavailableException(proider: String, status: Int = -1) : RuntimeException("Location Service is unavailable: $proider, status: $status")
