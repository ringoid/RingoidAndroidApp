package com.ringoid.data.local.database.di

import com.ringoid.data.local.database.facade.MessageDbFacadeImpl
import com.ringoid.datainterface.messenger.IMessageDbFacade
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FacadeModule {

    @Provides @Singleton
    fun provideMessageDbFacade(facade: MessageDbFacadeImpl): IMessageDbFacade = facade
}
