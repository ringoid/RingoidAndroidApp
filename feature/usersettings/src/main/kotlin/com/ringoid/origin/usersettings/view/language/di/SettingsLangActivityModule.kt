package com.ringoid.origin.usersettings.view.language.di

import com.ringoid.origin.usersettings.view.language.SettingsLangActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsLangActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsLangActivityInjector(): SettingsLangActivity
}
