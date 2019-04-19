package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo

@Database(version = 100, entities = [ActionObjectDbo::class])
abstract class BackupRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "BackupRingoid.db"
    }

    abstract fun actionObjectDao(): ActionObjectDao
}
