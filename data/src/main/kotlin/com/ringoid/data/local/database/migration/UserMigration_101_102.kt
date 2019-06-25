package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.messenger.MessageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMigration_101_102 @Inject constructor() : Migration(101, 102) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${MessageDbo.TABLE_NAME} ADD COLUMN ${MessageDbo.COLUMN_CLIENT_ID} TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE ${MessageDbo.TABLE_NAME} ADD COLUMN ${MessageDbo.COLUMN_TIMESTAMP} INTEGER NOT NULL DEFAULT 0")
    }
}
