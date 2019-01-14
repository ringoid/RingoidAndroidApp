package com.ringoid.usersettings.view.di

import com.ringoid.usersettings.view.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsActivityInjector(): SettingsActivity
}
