package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.image.UserImageDao
import com.ringoid.data.local.database.model.image.UserImageDbo

@Database(version = 100, entities = [UserImageDbo::class])
abstract class BackupUserRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "BackupUserRingoid.db"
    }

    abstract fun userImageDao(): UserImageDao
}
