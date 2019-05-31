package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.dao.image.ImageRequestDao
import com.ringoid.data.local.database.dao.image.UserImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.UserProfileDbo
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo

@Database(version = 101,
          entities = [ImageRequestDbo::class, MessageDbo::class,
                      ProfileIdDbo::class, UserImageDbo::class, UserProfileDbo::class])
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
