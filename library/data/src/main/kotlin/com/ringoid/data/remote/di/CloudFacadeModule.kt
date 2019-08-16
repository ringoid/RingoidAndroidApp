package com.ringoid.data.remote.di

import com.ringoid.data.remote.facade.RingoidCloudFacadeImpl
import com.ringoid.data.remote.facade.SystemCloudFacadeImpl
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.ISystemCloudFacade
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RingoidCloudModule::class, SystemCloudModule::class])
class CloudFacadeModule {

    @Provides @Singleton
    fun provideRingoidCloudFacade(facade: RingoidCloudFacadeImpl): IRingoidCloudFacade = facade

    @Provides @Singleton
    fun provideSystemCloudFacade(facade: SystemCloudFacadeImpl): ISystemCloudFacade = facade
}
