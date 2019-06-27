package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.dao.feed.FeedDao
import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo

@Database(version = 200,
          entities = [ActionObjectDbo::class, ImageDbo::class,
                      MessageDbo::class, FeedItemDbo::class])
abstract class RingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "Ringoid.db"
    }

    abstract fun actionObjectDao(): ActionObjectDao
    abstract fun feedDao(): FeedDao
    abstract fun imageDao(): ImageDao
    abstract fun messageDao(): MessageDao
}
