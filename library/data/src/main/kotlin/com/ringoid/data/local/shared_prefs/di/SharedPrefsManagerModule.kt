package com.ringoid.data.local.shared_prefs.di

import com.ringoid.config.IRuntimeConfig
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.manager.RuntimeConfig
import com.ringoid.domain.manager.ISharedPrefsManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPrefsManagerModule {

    @Provides @Singleton
    fun provideRuntimeConfig(config: RuntimeConfig): IRuntimeConfig = config

    @Provides @Singleton
    fun provideSharedPrefsManager(spm: SharedPrefsManager): ISharedPrefsManager = spm
}
