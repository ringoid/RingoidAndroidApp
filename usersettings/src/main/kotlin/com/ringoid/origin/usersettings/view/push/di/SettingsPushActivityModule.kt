package com.ringoid.origin.usersettings.view.push.di

import com.ringoid.origin.usersettings.view.push.SettingsPushActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsPushActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsPushActivityInjector(): SettingsPushActivity
}
