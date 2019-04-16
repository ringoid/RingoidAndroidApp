package com.ringoid.data.local.database.dao.action_storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectMigration_10_11 @Inject constructor() : Migration(10, 11) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_USED} INTEGER NOT NULL DEFAULT 0")
    }
}
