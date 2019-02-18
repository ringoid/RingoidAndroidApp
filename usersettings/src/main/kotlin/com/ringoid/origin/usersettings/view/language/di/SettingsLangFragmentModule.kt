package com.ringoid.origin.usersettings.view.language.di

import com.ringoid.origin.usersettings.view.language.SettingsLangFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsLangFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsLangFragmentInjector(): SettingsLangFragment
}
