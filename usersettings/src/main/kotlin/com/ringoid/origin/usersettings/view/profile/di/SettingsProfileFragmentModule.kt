package com.ringoid.origin.usersettings.view.profile.di

import com.ringoid.origin.usersettings.view.profile.SettingsProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsProfileFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsProfileFragmentInjector(): SettingsProfileFragment
}
