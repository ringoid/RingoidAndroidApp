package com.ringoid.data.local.database.dao.messenger

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.messenger.MessageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_9_10 @Inject constructor() : Migration(9, 10) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${MessageDbo.TABLE_NAME} ADD COLUMN ${MessageDbo.COLUMN_UNREAD} INTEGER NOT NULL DEFAULT 1")
    }
}
