package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo

@Database(version = 1, entities = [ProfileIdDbo::class])
abstract class BlockProfilesUserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "BlockProfilesUserRingoid.db"
    }

    abstract fun userFeedDao(): UserFeedDao
}
