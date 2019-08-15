package com.ringoid.utility.manager

import android.content.Context
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTimeZone
import java.util.*

class TimezoneManager(context: Context) {

    init {
        JodaTimeAndroid.init(context)
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun getTimeZone(): Int = DateTimeZone.forTimeZone(TimeZone.getDefault()).getOffset(null) / 3600_000
}
