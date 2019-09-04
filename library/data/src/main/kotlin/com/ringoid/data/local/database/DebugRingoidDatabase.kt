package com.ringoid.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ringoid.data.local.database.dao.debug.BarrierLogDao
import com.ringoid.data.local.database.dao.debug.DebugLogDao
import com.ringoid.data.local.database.model.debug.BarrierLogItemDbo
import com.ringoid.data.local.database.model.debug.DebugLogItemDbo
import com.ringoid.utility.DebugOnly

@DebugOnly
@Database(version = 101, entities = [BarrierLogItemDbo::class, DebugLogItemDbo::class])
abstract class DebugRingoidDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "DebugRingoid.db"
    }

    abstract fun barrierLogDao(): BarrierLogDao
    abstract fun debugLogDao(): DebugLogDao
}
