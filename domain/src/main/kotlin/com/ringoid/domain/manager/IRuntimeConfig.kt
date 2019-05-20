package com.ringoid.domain.manager

interface IRuntimeConfig {

    fun isDeveloper(): Boolean
    fun collectDebugLogs(): Boolean
}
