package com.ringoid.data.local.database.di

import com.ringoid.data.local.database.facade.feed.FeedDbFacadeImpl
import com.ringoid.data.local.database.facade.image.ImageDbFacadeImpl
import com.ringoid.data.local.database.facade.messenger.MessageDbFacadeImpl
import com.ringoid.datainterface.feed.IFeedDbFacade
import com.ringoid.datainterface.image.IImageDbFacade
import com.ringoid.datainterface.messenger.IMessageDbFacade
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FacadeModule {

    @Provides @Singleton
    fun provideFeedDbFacade(facade: FeedDbFacadeImpl): IFeedDbFacade = facade

    @Provides @Singleton
    fun provideImageDbFacade(facade: ImageDbFacadeImpl): IImageDbFacade = facade

    @Provides @Singleton
    fun provideMessageDbFacade(facade: MessageDbFacadeImpl): IMessageDbFacade = facade
}
