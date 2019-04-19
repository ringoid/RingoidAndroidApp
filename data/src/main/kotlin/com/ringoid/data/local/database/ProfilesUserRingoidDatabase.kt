package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo

@Database(version = 100, entities = [ProfileIdDbo::class])
abstract class BlockProfilesUserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "BlockProfilesUserRingoid.db"
    }

    abstract fun userFeedDao(): UserFeedDao
}

@Database(version = 100, entities = [ProfileIdDbo::class])
abstract class NewLikesProfilesUserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "NewLikesProfilesUserRingoid.db"
    }

    abstract fun userFeedDao(): UserFeedDao
}

@Database(version = 100, entities = [ProfileIdDbo::class])
abstract class NewMatchesProfilesUserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "NewMatchesProfilesUserRingoid.db"
    }

    abstract fun userFeedDao(): UserFeedDao
}
