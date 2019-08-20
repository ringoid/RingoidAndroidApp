package com.ringoid.utility

import android.content.Context
import android.net.ConnectivityManager

fun Context.isNetworkAvailable(): Boolean =
    connectivityManager()
        ?.activeNetworkInfo
        ?.let { it.isConnectedOrConnecting }
        ?: false

fun Context.connectivityManager(): ConnectivityManager? =
    getSystemService(Context.CONNECTIVITY_SERVICE)
        ?.let { it as? ConnectivityManager }
