package com.ringoid.config

interface IRuntimeConfig {

    fun isDeveloper(): Boolean
    fun collectDebugLogs(): Boolean
}
