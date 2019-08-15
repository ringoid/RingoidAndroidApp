package com.ringoid.origin.usersettings.view.filters.di

import com.ringoid.origin.usersettings.view.filters.SettingsFiltersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFiltersFragmentModule  {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFiltersFragmentInjector(): SettingsFiltersFragment
}
