package com.ringoid.imageloader

import android.util.Log
import android.view.View
import timber.log.Timber

fun View.logv(message: String) { log(message, Log.VERBOSE) }
fun View.loge(error: Throwable, message: String) { log(error, message, Log.ERROR) }

fun View.log(message: String, severity: Int) {
    post { Timber.log(severity, message) }
}

fun View.log(error: Throwable, message: String, severity: Int) {
    post { Timber.log(severity, error, message) }
}
