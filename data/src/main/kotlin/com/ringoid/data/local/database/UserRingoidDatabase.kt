package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.user.UserDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo

@Database(version = 1,
          entities = [ImageDbo::class, ProfileDbo::class, UserImageDbo::class])
abstract class UserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "UserRingoid.db"
    }

    abstract fun imageDao(): ImageDao
    abstract fun userDao(): UserDao
}
