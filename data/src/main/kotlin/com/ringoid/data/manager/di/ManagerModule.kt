package com.ringoid.data.manager.di

import android.content.Context
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.utility.manager.TimezoneManager
import com.ringoid.data.manager.UserSettingsManager
import com.ringoid.domain.manager.IUserSettingsManager
import com.ringoid.utility.manager.LocaleManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ManagerModule {

    @Provides @Singleton
    fun provideLocaleManager(context: Context): LocaleManager = LocaleManager(context)

    @Provides @Singleton
    fun provideUserSettingsManager(localeManager: LocaleManager, spm: SharedPrefsManager, timezoneManager: TimezoneManager)
            : IUserSettingsManager = UserSettingsManager(localeManager, spm, timezoneManager)

    fun provideTimezoneManager(context: Context): TimezoneManager = TimezoneManager(context)
}
