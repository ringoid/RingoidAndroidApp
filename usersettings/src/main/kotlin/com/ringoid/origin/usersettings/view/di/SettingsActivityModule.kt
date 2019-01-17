package com.ringoid.origin.usersettings.view.di

import com.ringoid.origin.usersettings.view.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsActivityInjector(): SettingsActivity
}
