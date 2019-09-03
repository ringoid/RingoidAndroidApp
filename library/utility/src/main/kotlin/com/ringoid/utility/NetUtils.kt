package com.ringoid.utility

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException

fun Context.isNetworkAvailable(): Boolean =
    connectivityManager()
        ?.activeNetworkInfo
        ?.let { it.isConnectedOrConnecting }
        ?: false

fun Context.connectivityManager(): ConnectivityManager? =
    getSystemService(Context.CONNECTIVITY_SERVICE)
        ?.let { it as? ConnectivityManager }

fun Throwable.isNotFoundNetworkError(): Boolean =
    if (this is IOException) {
        message?.let { it.contains("code=404") || it.contains("Not Found") }
               ?: false
    } else false
