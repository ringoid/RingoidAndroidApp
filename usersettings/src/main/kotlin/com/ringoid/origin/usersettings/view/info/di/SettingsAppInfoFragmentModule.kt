package com.ringoid.origin.usersettings.view.info.di

import com.ringoid.origin.usersettings.view.info.SettingsAppInfoFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsAppInfoFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsAppInfoFragmentInjector(): SettingsAppInfoFragment
}
