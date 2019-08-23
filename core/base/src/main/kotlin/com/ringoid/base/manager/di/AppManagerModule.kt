package com.ringoid.base.manager.di

import com.ringoid.base.manager.location.ILocationProvider
import com.ringoid.base.manager.location.SingleShotLocationProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppManagerModule {

    @Provides @Singleton
    fun provideLocationProvider(provider: SingleShotLocationProvider): ILocationProvider = provider
}
