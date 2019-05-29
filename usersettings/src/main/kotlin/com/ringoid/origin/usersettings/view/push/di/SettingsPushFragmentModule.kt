package com.ringoid.origin.usersettings.view.push.di

import com.ringoid.origin.usersettings.view.push.SettingsPushFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsPushFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsPushFragmentInjector(): SettingsPushFragment
}
