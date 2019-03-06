package com.ringoid.utility

import java.util.*

fun isAdultAge(yearOfBirth: Int, calendar: Calendar, adultThreshold: Int = 18): Boolean =
    yearOfBirth in 1938..(calendar.get(Calendar.YEAR) - adultThreshold)

fun Date.time(): String = "${wrapTimeUnit(hours)}:${wrapTimeUnit(minutes)}:${wrapTimeUnit(seconds)}"

fun wrapTimeUnit(unit: Int): String = if (unit < 10) "0$unit" else "$unit"
fun wrapMillisUnit(unit: Long): String = if (unit < 10) "00$unit" else if (unit < 100) "0$unit" else "$unit"
