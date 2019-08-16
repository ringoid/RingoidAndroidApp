package com.ringoid.data.local.database.di

import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.facade.feed.FeedDbFacadeImpl
import com.ringoid.data.local.database.facade.image.ImageDbFacadeImpl
import com.ringoid.data.local.database.facade.messenger.MessageDbFacadeImpl
import com.ringoid.data.local.database.facade.user.UserDbFacadeImpl
import com.ringoid.data.local.database.facade.user.UserFeedDbFacadeImpl
import com.ringoid.data.local.database.facade.user.UserImageDbFacadeImpl
import com.ringoid.datainterface.di.*
import com.ringoid.datainterface.feed.IFeedDbFacade
import com.ringoid.datainterface.image.IImageDbFacade
import com.ringoid.datainterface.messenger.IMessageDbFacade
import com.ringoid.datainterface.user.IUserDbFacade
import com.ringoid.datainterface.user.IUserFeedDbFacade
import com.ringoid.datainterface.user.IUserImageDbFacade
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

    @Provides @Singleton @PerAlreadySeen
    fun provideAlreadySeenUserFeedDbFacade(@PerAlreadySeen dao: UserFeedDao): IUserFeedDbFacade = UserFeedDbFacadeImpl(dao)

    @Provides @Singleton @PerBlock
    fun provideBlockedUserFeedDbFacade(@PerBlock dao: UserFeedDao): IUserFeedDbFacade = UserFeedDbFacadeImpl(dao)

    @Provides @Singleton @PerLmmLikes
    fun provideNewLikesUserFeedDbFacade(@PerLmmLikes dao: UserFeedDao): IUserFeedDbFacade = UserFeedDbFacadeImpl(dao)

    @Provides @Singleton @PerLmmMatches
    fun provideNewMatchesUserFeedDbFacade(@PerLmmMatches dao: UserFeedDao): IUserFeedDbFacade = UserFeedDbFacadeImpl(dao)

    @Provides @Singleton @PerUser
    fun provideUserDbFacade(@PerUser dao: UserDao): IUserDbFacade = UserDbFacadeImpl(dao)

    @Provides @Singleton
    fun provideUserImageDbFacade(facade: UserImageDbFacadeImpl): IUserImageDbFacade = facade

    @Provides @Singleton @PerUser
    fun provideUserMessageDbFacade(@PerUser dao: MessageDao): IMessageDbFacade = MessageDbFacadeImpl(dao)
}
