package com.ringoid.utility

import android.content.Context
import android.net.ConnectivityManager

fun Context.isNetworkAvailable(): Boolean =
    getSystemService(Context.CONNECTIVITY_SERVICE)
        ?.let { it as? ConnectivityManager }
        ?.let { it.activeNetworkInfo }
        ?.let { it.isConnectedOrConnecting }
        ?: false
