package com.ringoid.usersettings.view.di

import com.ringoid.usersettings.view.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragmentInjector(): SettingsFragment
}
