package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.debug.DebugLogDao
import com.ringoid.data.local.database.model.debug.DebugLogItemDbo
import com.ringoid.domain.debug.DebugOnly

@DebugOnly
@Database(version = 100, entities = [DebugLogItemDbo::class])
abstract class DebugRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "DebugRingoid.db"
    }

    abstract fun debugLogDao(): DebugLogDao
}
