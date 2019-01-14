package com.ringoid.usersettings.view.info.di

import com.ringoid.usersettings.view.info.SettingsAppInfoActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsAppInfoActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsAppInfoActivityInjector(): SettingsAppInfoActivity
}
