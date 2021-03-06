package com.ringoid.data.local.database.di

import android.content.Context
import androidx.room.Room
import com.ringoid.data.local.database.*
import com.ringoid.data.local.database.dao.messenger.MessageMigration_9_10
import com.ringoid.data.local.database.migration.*
import com.ringoid.utility.DebugOnly
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(applicationContext: Context,
                        migration_100_101: Migration_100_101,
                        migration_101_102: Migration_101_102,
                        migration_102_103: Migration_102_103,
                        migration_103_104: Migration_103_104,
                        migration_104_105: Migration_104_105,
                        migration_105_106: Migration_105_106,
                        migration_106_107: Migration_106_107,
                        majorMigration_107_200: MajorMigration_107_200,
                        migration_200_201: Migration_200_201,
                        migration_201_202: Migration_201_202,
                        migration_202_203: Migration_202_203,
                        migration_203_204: Migration_203_204,
                        migration_204_205: Migration_204_205): RingoidDatabase =
        Room.databaseBuilder(applicationContext, RingoidDatabase::class.java, RingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_100_101, migration_101_102, migration_102_103, migration_103_104, migration_104_105,
                           migration_105_106, migration_106_107, majorMigration_107_200,
                           migration_200_201, migration_201_202, migration_202_203,
                           migration_203_204, migration_204_205)
            .fallbackToDestructiveMigrationFrom(8, 9, 10)
            .build()

    @Provides @Singleton
    fun provideUserDatabase(applicationContext: Context,
                            migration_9_10: MessageMigration_9_10,
                            majorMigration_10_100: MajorMigration_10_100,
                            migration_100_101: UserMigration_100_101,
                            migration_101_102: UserMigration_101_102,
                            majorMigration_102_200: UserMigration_102_200): UserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UserRingoidDatabase::class.java, UserRingoidDatabase.DATABASE_NAME)
            .addMigrations(migration_9_10, majorMigration_10_100, migration_100_101, migration_101_102, majorMigration_102_200)
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
    fun provideUnreadChatsUserRingoidDatabase(applicationContext: Context): UnreadChatsUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, UnreadChatsUserRingoidDatabase::class.java, UnreadChatsUserRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(1)
            .build()

    @Provides @Singleton
    fun provideBackupRingoidDatabase(applicationContext: Context): BackupRingoidDatabase =
        Room.databaseBuilder(applicationContext, BackupRingoidDatabase::class.java, BackupRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigrationFrom(3)
            .build()

    @Provides @Singleton
    fun provideBackupUserRingoidDatabase(applicationContext: Context): BackupUserRingoidDatabase =
        Room.databaseBuilder(applicationContext, BackupUserRingoidDatabase::class.java, BackupUserRingoidDatabase.DATABASE_NAME)
            .build()

    @Provides @Singleton @DebugOnly
    fun provideDebugRingoidDatabase(applicationContext: Context): DebugRingoidDatabase =
        Room.databaseBuilder(applicationContext, DebugRingoidDatabase::class.java, DebugRingoidDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
}
