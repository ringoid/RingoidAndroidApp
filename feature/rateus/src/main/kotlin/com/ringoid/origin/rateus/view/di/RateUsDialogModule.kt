package com.ringoid.origin.rateus.view.di

import com.ringoid.origin.rateus.view.RateUsDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class RateUsDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeRateUsDialogInjector(): RateUsDialog
}
