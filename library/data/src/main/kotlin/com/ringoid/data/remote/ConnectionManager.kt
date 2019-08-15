package com.ringoid.data.remote

import android.content.Context
import android.net.ConnectivityManager
import com.ringoid.domain.manager.IConnectionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor(private val context: Context)
    : IConnectionManager {

    override fun isNetworkAvailable(): Boolean =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
            ?.let { it as? ConnectivityManager }
            ?.let { it.activeNetworkInfo }
            ?.let { it.isConnectedOrConnecting }
            ?: false
}
