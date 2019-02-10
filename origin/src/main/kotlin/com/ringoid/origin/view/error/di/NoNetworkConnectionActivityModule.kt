package com.ringoid.origin.view.error.di

import com.ringoid.origin.view.error.NoNetworkConnectionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class NoNetworkConnectionActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeNoNetworkConnectionActivityInjector(): NoNetworkConnectionActivity
}
