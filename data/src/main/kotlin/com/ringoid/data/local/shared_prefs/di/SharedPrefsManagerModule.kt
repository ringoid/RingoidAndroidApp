package com.ringoid.data.local.shared_prefs.di

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.domain.repository.ISharedPrefsManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPrefsManagerModule {

    @Provides @Singleton
    fun provideSharedPrefsManager(spm: SharedPrefsManager): ISharedPrefsManager = spm
}
