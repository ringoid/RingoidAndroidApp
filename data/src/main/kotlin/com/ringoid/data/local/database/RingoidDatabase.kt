package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo

@Database(version = 3,
          entities = [ImageDbo::class, MessageDbo::class,
                      ProfileDbo::class, UserImageDbo::class])
abstract class RingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "Ringoid.db"
    }

    abstract fun feedDao(): FeedDao
    abstract fun imageDao(): ImageDao
    abstract fun messageDao(): MessageDao
}
