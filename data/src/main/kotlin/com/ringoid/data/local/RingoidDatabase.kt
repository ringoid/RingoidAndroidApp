package com.ringoid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.dao.feed.ProfileDao
import com.ringoid.data.local.model.feed.ProfileDbo
import com.ringoid.data.local.model.image.BaseImageDbo

@Database(entities = [BaseImageDbo::class, ProfileDbo::class], version = 1)
abstract class RingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "Ringoid.db"
    }

    abstract fun profileDao(): ProfileDao
}
