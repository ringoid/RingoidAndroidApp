package com.ringoid.origin.usersettings.view.info.di

import com.ringoid.origin.usersettings.view.info.SettingsAppInfoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsAppInfoActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsAppInfoActivityInjector(): SettingsAppInfoActivity
}
