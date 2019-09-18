package com.ringoid.data.local.database.dao.messenger

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.model.messenger.UNREAD_BY_USER
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageMigration_9_10 @Inject constructor() : Migration(9, 10) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${MessageDbo.TABLE_NAME} ADD COLUMN ${MessageDbo.COLUMN_READ_STATUS} INTEGER NOT NULL DEFAULT $UNREAD_BY_USER")
    }
}
