package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.local.database.RingoidDatabase
import com.ringoid.data.local.database.dao.image.ImageDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideImageDao(database: RingoidDatabase): ImageDao = database.imageDao()
}
