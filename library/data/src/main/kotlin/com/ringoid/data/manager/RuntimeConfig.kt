package com.ringoid.data.manager

import com.ringoid.config.IRuntimeConfig
import com.ringoid.domain.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuntimeConfig @Inject constructor() : IRuntimeConfig {

    private var isDeveloper: Boolean = BuildConfig.IS_STAGING
    private var collectLogs: Boolean = BuildConfig.IS_STAGING

    override fun isDeveloper(): Boolean = isDeveloper

    override fun collectDebugLogs(): Boolean = collectLogs

    internal fun setDeveloperMode(flag: Boolean) {
        isDeveloper = flag
    }

    internal fun setCollectDebugLogs(flag: Boolean) {
        collectLogs = flag
    }
}
