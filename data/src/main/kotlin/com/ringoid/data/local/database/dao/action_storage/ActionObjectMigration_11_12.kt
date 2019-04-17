package com.ringoid.data.local.database.dao.action_storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectMigration_11_12 @Inject constructor() : Migration(11, 12) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_LOCATION_LATITUDE} REAL NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_LOCATION_LONGITUDE} REAL NOT NULL DEFAULT 0")
    }
}
