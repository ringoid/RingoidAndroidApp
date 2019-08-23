package com.ringoid.origin.usersettings.view.debug.di

import com.ringoid.origin.usersettings.view.debug.DebugFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DebugFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeDebugFragmentInjector(): DebugFragment
}
