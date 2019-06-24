package com.ringoid.utility

import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import java.util.*

fun isAdultAge(yearOfBirth: Int, calendar: Calendar, adultThreshold: Int = 0): Boolean =
    yearOfBirth in 1919..(calendar.get(Calendar.YEAR) - adultThreshold)

fun Date.date(): String = "${1900 + year}.${wrapTimeUnit(month + 1)}.${wrapTimeUnit(date)}"
fun Date.time(): String = "${wrapTimeUnit(hours)}:${wrapTimeUnit(minutes)}:${wrapTimeUnit(seconds)}"

fun daysAgo(ts: Long): String =
    Period(DateTime(ts), DateTime.now())
        .let { period ->
            PeriodFormatterBuilder()
                .appendDays().appendSuffix(" days ago")
                .toFormatter()
                .print(period)
        }

fun fromTs(ts: Long): String =
    DateTime(ts).let { dt -> "${dt.year()}-${dt.monthOfYear()}-${dt.dayOfMonth()}" }


fun wrapTimeUnit(unit: Int): String = if (unit < 10) "0$unit" else "$unit"
fun wrapMillisUnit(unit: Long): String = if (unit < 10) "00$unit" else if (unit < 100) "0$unit" else "$unit"
