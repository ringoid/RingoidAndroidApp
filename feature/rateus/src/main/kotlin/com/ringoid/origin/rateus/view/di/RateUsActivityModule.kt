package com.ringoid.origin.rateus.view.di

import com.ringoid.origin.rateus.view.RateUsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RateUsActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeRateUsActivityInjector(): RateUsActivity
}
