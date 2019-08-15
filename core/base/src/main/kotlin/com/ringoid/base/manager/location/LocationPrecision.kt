package com.ringoid.base.manager.location

enum class LocationPrecision {
    ANY, COARSE, FINE;

    companion object {
        val values = LocationPrecision.values()
    }
}
