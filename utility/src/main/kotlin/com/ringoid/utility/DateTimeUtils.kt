package com.ringoid.utility

import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*

fun age(yearOfBirth: Int, calendar: Calendar): Int =
    calendar.get(Calendar.YEAR) - yearOfBirth

fun isAdultAge(yearOfBirth: Int, calendar: Calendar, adultThreshold: Int = 18): Boolean =
    yearOfBirth in 1919..(calendar.get(Calendar.YEAR) - adultThreshold)

fun Date.date(): String = "${1900 + year}.${wrapTimeUnit(month + 1)}.${wrapTimeUnit(date)}"
fun Date.time(): String = "${wrapTimeUnit(hours)}:${wrapTimeUnit(minutes)}:${wrapTimeUnit(seconds)}"

fun daysAgo(ts: Long): String =
    Days.daysBetween(DateTime(ts), DateTime.now()).let { "${it.days} days ago" }

fun fromTs(ts: Long): String =
    DateTime(ts).let { dt -> "${dt.year().get()}-${dt.monthOfYear().get()}-${dt.dayOfMonth().get()}" }


fun wrapTimeUnit(unit: Int): String = if (unit < 10) "0$unit" else "$unit"
fun wrapMillisUnit(unit: Long): String = if (unit < 10) "00$unit" else if (unit < 100) "0$unit" else "$unit"
