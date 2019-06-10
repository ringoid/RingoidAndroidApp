package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.remote.model.feed.BaseProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_102_103 @Inject constructor() : Migration(102, 103) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_AGE} INTEGER NOT NULL DEFAULT 0")
    }
}
