package com.ringoid.origin.usersettings.view.settings.di

import com.ringoid.origin.usersettings.view.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsActivityInjector(): SettingsActivity
}
