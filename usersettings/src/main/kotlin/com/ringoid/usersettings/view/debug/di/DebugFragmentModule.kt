package com.ringoid.usersettings.view.debug.di

import com.ringoid.usersettings.view.debug.DebugFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DebugFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDebugFragmentInjector(): DebugFragment
}
