package com.ringoid.utility

object ValueUtils {

    fun isValidLocation(latitude: Double, longitude: Double): Boolean =
        latitude != 0.0 || longitude != 0.0
}
