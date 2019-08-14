package com.ringoid.origin.usersettings.view.filters.di

import com.ringoid.origin.usersettings.view.filters.SettingsFiltersActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFiltersActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFiltersActivityInjector(): SettingsFiltersActivity
}
