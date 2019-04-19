package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.dao.image.UserImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo
import javax.inject.Inject

@Database(version = 100,
          entities = [ImageRequestDbo::class, MessageDbo::class,
                      ProfileDbo::class, ProfileIdDbo::class, UserImageDbo::class])
abstract class UserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "UserRingoid.db"
    }

    abstract fun imageRequestDao(): ImageRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun userFeedDao(): UserFeedDao
    abstract fun userImageDao(): UserImageDao
}

class MajorMigration_10_100 @Inject constructor() : Migration(10, 100) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS ${ImageDbo.TABLE_NAME}")
    }
}
