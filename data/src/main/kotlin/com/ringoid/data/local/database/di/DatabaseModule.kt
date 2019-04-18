package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.local.database.*
import com.ringoid.data.local.database.dao.messenger.MessageMigration_9_10
import com.ringoid.domain.debug.DebugOnly
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
//            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 100)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideUserDatabase(applicationContext: Context,
                            migration_9_10: MessageMigration_9_10,
                            majorMigration_10_100: MajorMigration_10_100): UserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UserRingoidDatabase::class.java, UserRingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_9_10, majorMigration_10_100)
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
}
