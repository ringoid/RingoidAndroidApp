package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.di.*
import com.ringoid.data.local.database.*
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.dao.action_storage.ActionObjectMigration_10_11
import com.ringoid.data.local.database.dao.action_storage.ActionObjectMigration_11_12
import com.ringoid.data.local.database.dao.debug.DebugLogDao
import com.ringoid.data.local.database.dao.debug.DebugLogDaoHelper
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.messenger.MessageMigration_9_10
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.IDebugLogDaoHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context, migration_9_10: MessageMigration_9_10,
                        migration_10_11: ActionObjectMigration_10_11,
                        migration_11_12: ActionObjectMigration_11_12): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_9_10, migration_10_11, migration_11_12)
            .build()

    @Provides @Singleton
    fun provideUserDatabase(applicationContext: Context, migration_9_10: MessageMigration_9_10): UserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UserRingoidDatabase::class.java, UserRingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_9_10)
            .build()

    @Provides @Singleton
    fun provideBlockProfilesUserDatabase(applicationContext: Context): BlockProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, BlockProfilesUserRingoidDatabase::class.java, BlockProfilesUserRingoidDatabase.DATABASE_NAME)
            .build()

    @Provides @Singleton
    fun provideNewLikesProfilesUserDatabase(applicationContext: Context): NewLikesProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, NewLikesProfilesUserRingoidDatabase::class.java, NewLikesProfilesUserRingoidDatabase.DATABASE_NAME)
            .build()

    @Provides @Singleton
    fun provideNewMatchesProfilesUserDatabase(applicationContext: Context): NewMatchesProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, NewMatchesProfilesUserRingoidDatabase::class.java, NewMatchesProfilesUserRingoidDatabase.DATABASE_NAME)
            .build()

    @Provides @Singleton
    fun provideBackupRingoidDatabase(applicationContext: Context): BackupRingoidDatabase =
        Room.databaseBuilder(applicationContext, BackupRingoidDatabase::class.java, BackupRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton @DebugOnly
    fun provideDebugRingoidDatabase(applicationContext: Context): DebugRingoidDatabase =
        Room.databaseBuilder(applicationContext, DebugRingoidDatabase::class.java, DebugRingoidDatabase.DATABASE_NAME)
            .build()

    // ------------------------------------------
    @Provides @Singleton
    fun provideActionObjectDao(database: RingoidDatabase): ActionObjectDao = database.actionObjectDao()

    @Provides @Singleton @PerBackup
    fun provideBackupActionObjectDao(database: BackupRingoidDatabase): ActionObjectDao = database.actionObjectDao()

    @Provides @Singleton
    fun provideFeedDao(database: RingoidDatabase): FeedDao = database.feedDao()

    @Provides @Singleton @PerFeed
    fun provideFeedImageDao(database: RingoidDatabase): ImageDao = database.imageDao()

    @Provides @Singleton @PerUser
    fun provideUserImageDao(database: UserRingoidDatabase): ImageDao = database.imageDao()

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
    fun provideDebugLogDao(database: DebugRingoidDatabase): DebugLogDao = database.debugLogDao()

    @Provides @Singleton @DebugOnly
    fun provideDebugLogDaoHelper(daoHelper: DebugLogDaoHelper): IDebugLogDaoHelper = daoHelper
}
