package com.ringoid.domain.manager

import android.content.Context
import android.net.ConnectivityManager
import javax.inject.Inject

class ConnectionManager @Inject constructor(private val context: Context) {

    fun isNetworkAvailable(): Boolean =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
            ?.let { it as? ConnectivityManager }
            ?.let { it.activeNetworkInfo }
            ?.let { it.isConnectedOrConnecting }
            ?: false
}
