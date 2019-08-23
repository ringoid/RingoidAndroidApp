package com.ringoid.origin.view.splash.di

import com.ringoid.origin.view.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SplashActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeSplashActivityInjector(): SplashActivity
}
