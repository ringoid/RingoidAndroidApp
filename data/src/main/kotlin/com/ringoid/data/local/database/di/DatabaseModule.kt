package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.local.database.RingoidDatabase
import com.ringoid.data.local.database.UserRingoidDatabase
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideUserDatabase(applicationContext: Context): UserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UserRingoidDatabase::class.java, UserRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideFeedDao(database: RingoidDatabase): FeedDao = database.feedDao()

    @Provides @Singleton @Named("feed")
    fun provideFeedImageDao(database: RingoidDatabase): ImageDao = database.imageDao()

    @Provides @Singleton @Named("user")
    fun provideUserImageDao(database: UserRingoidDatabase): ImageDao = database.imageDao()

    @Provides @Singleton
    fun provideMessageDao(database: RingoidDatabase): MessageDao = database.messageDao()

    @Provides @Singleton @Named("user")
    fun provideUserDao(database: UserRingoidDatabase): UserDao = database.userDao()
}
