package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo

@Database(version = 10,
          entities = [ImageDbo::class, ImageRequestDbo::class, MessageDbo::class,
                      ProfileDbo::class, ProfileIdDbo::class, UserImageDbo::class])
abstract class UserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "UserRingoid.db"
    }

    abstract fun imageDao(): ImageDao
    abstract fun imageRequestDao(): ImageRequestDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun userFeedDao(): UserFeedDao
}
