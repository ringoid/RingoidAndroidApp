package com.ringoid.data.local.database.di

import com.ringoid.data.di.*
import com.ringoid.data.local.database.*
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.dao.debug.BarrierLogDao
import com.ringoid.data.local.database.dao.debug.BarrierLogDaoHelper
import com.ringoid.data.local.database.dao.debug.DebugLogDao
import com.ringoid.data.local.database.dao.debug.DebugLogDaoHelper
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.dao.image.UserImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.IBarrierLogDaoHelper
import com.ringoid.domain.debug.IDebugLogDaoHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [DatabaseModule::class])
class DaoModule {

    @Provides @Singleton
    fun provideActionObjectDao(database: RingoidDatabase): ActionObjectDao = database.actionObjectDao()

    @Provides @Singleton @PerBackup
    fun provideBackupActionObjectDao(database: BackupRingoidDatabase): ActionObjectDao = database.actionObjectDao()

    @Provides @Singleton
    fun provideFeedDao(database: RingoidDatabase): FeedDao = database.feedDao()

    @Provides @Singleton
    fun provideFeedImageDao(database: RingoidDatabase): ImageDao = database.imageDao()

    @Provides @Singleton
    fun provideUserImageDao(database: UserRingoidDatabase): UserImageDao = database.userImageDao()

    @Provides @Singleton @PerUser
    fun provideUserImageRequestDao(database: UserRingoidDatabase): ImageRequestDao = database.imageRequestDao()

    @Provides @Singleton
    fun provideMessageDao(database: RingoidDatabase): MessageDao = database.messageDao()

    @Provides @Singleton @PerUser
    fun provideUserDao(database: UserRingoidDatabase): UserDao = database.userDao()

    @Provides @Singleton @PerUser
    fun provideUserMessageDao(database: UserRingoidDatabase): MessageDao = database.messageDao()

    @Provides @Singleton @PerAlreadySeen
    fun provideAlreadySeenUserFeedDao(database: UserRingoidDatabase): UserFeedDao = database.userFeedDao()

    @Provides @Singleton @PerBlock
    fun provideBlockedUserFeedDao(database: BlockProfilesUserRingoidDatabase): UserFeedDao = database.userFeedDao()

    @Provides @Singleton @PerLmmLikes
    fun provideNewLikesUserFeedDao(database: NewLikesProfilesUserRingoidDatabase): UserFeedDao = database.userFeedDao()

    @Provides @Singleton @PerLmmMatches
    fun provideNewMatchesUserFeedDao(database: NewMatchesProfilesUserRingoidDatabase): UserFeedDao = database.userFeedDao()

    @Provides @Singleton @DebugOnly
    fun provideBarrierLogDao(database: DebugRingoidDatabase): BarrierLogDao = database.barrierLogDao()

    @Provides @Singleton @DebugOnly
    fun provideDebugLogDao(database: DebugRingoidDatabase): DebugLogDao = database.debugLogDao()

    @Provides @Singleton @DebugOnly
    fun provideBarrierLogDaoHelper(daoHelper: BarrierLogDaoHelper): IBarrierLogDaoHelper = daoHelper

    @Provides @Singleton @DebugOnly
    fun provideDebugLogDaoHelper(daoHelper: DebugLogDaoHelper): IDebugLogDaoHelper = daoHelper
}
