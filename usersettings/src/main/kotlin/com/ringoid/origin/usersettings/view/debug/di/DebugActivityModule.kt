package com.ringoid.origin.usersettings.view.debug.di

import com.ringoid.origin.usersettings.view.debug.DebugActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DebugActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeDebugActivityInjector(): DebugActivity
}
