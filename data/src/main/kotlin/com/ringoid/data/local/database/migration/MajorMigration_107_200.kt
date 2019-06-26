package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.messenger.MessageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MajorMigration_107_200 @Inject constructor() : Migration(107, 200) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM ${MessageDbo.TABLE_NAME}")
    }
}
