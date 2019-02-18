package com.ringoid.origin.usersettings.view.settings.di

import com.ringoid.origin.usersettings.view.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragmentInjector(): SettingsFragment
}
