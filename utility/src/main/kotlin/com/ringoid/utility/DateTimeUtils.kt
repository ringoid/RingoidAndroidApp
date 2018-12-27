package com.ringoid.utility

import java.util.*

fun isAdultAge(yearOfBirth: Int, calendar: Calendar, adultThreshold: Int = 18): Boolean =
    yearOfBirth in 1938..(calendar.get(Calendar.YEAR) - adultThreshold)
