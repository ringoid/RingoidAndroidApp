package com.ringoid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.dao.feed.FeedDao
import com.ringoid.data.local.dao.image.ImageDao
import com.ringoid.data.local.dao.messenger.MessageDao
import com.ringoid.data.local.model.feed.ProfileDbo
import com.ringoid.data.local.model.image.ImageDbo
import com.ringoid.data.local.model.messenger.MessageDbo

@Database(entities = [ImageDbo::class, MessageDbo::class, ProfileDbo::class], version = 1)
abstract class RingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "Ringoid.db"
    }

    abstract fun feedDao(): FeedDao
    abstract fun imageDao(): ImageDao
    abstract fun messageDao(): MessageDao
}
