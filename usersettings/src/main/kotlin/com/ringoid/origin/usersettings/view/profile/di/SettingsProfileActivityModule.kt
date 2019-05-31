package com.ringoid.origin.usersettings.view.profile.di

import com.ringoid.origin.usersettings.view.profile.SettingsProfileActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsProfileActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsProfileActivityInjector(): SettingsProfileActivity
}
