package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.local.database.*
import com.ringoid.data.local.database.dao.messenger.MessageMigration_9_10
import com.ringoid.data.local.database.migration.MajorMigration_10_100
import com.ringoid.data.local.database.migration.Migration_100_101
import com.ringoid.data.local.database.migration.UserMigration_100_101
import com.ringoid.domain.debug.DebugOnly
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context,
                        migration_100_101: Migration_100_101): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_100_101)
            .fallbackToDestructiveMigrationFrom(8, 9, 10)
            .build()

    @Provides @Singleton
    fun provideUserDatabase(applicationContext: Context,
                            migration_9_10: MessageMigration_9_10,
                            majorMigration_10_100: MajorMigration_10_100,
                            migration_100_101: UserMigration_100_101): UserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UserRingoidDatabase::class.java, UserRingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_9_10, majorMigration_10_100, migration_100_101)
            .build()

    @Provides @Singleton
    fun provideBlockProfilesUserDatabase(applicationContext: Context): BlockProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, BlockProfilesUserRingoidDatabase::class.java, BlockProfilesUserRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides @Singleton
    fun provideNewLikesProfilesUserDatabase(applicationContext: Context): NewLikesProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, NewLikesProfilesUserRingoidDatabase::class.java, NewLikesProfilesUserRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides @Singleton
    fun provideNewMatchesProfilesUserDatabase(applicationContext: Context): NewMatchesProfilesUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, NewMatchesProfilesUserRingoidDatabase::class.java, NewMatchesProfilesUserRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides @Singleton
    fun provideBackupRingoidDatabase(applicationContext: Context): BackupRingoidDatabase =
        Room.databaseBuilder(applicationContext, BackupRingoidDatabase::class.java, BackupRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(3)
            .build()

    @Provides @Singleton @DebugOnly
    fun provideDebugRingoidDatabase(applicationContext: Context): DebugRingoidDatabase =
        Room.databaseBuilder(applicationContext, DebugRingoidDatabase::class.java, DebugRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(1)
            .build()
}
