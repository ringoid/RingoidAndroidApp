package com.ringoid.data.remote

import android.content.Context
import com.ringoid.domain.manager.IConnectionManager
import com.ringoid.utility.isNetworkAvailable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionManager @Inject constructor(private val context: Context)
    : IConnectionManager {

    override fun isNetworkAvailable(): Boolean = context.isNetworkAvailable()
}
