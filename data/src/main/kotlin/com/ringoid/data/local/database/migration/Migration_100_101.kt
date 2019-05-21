package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.image.BaseImageDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_100_101 @Inject constructor() : Migration(100, 101) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${ImageDbo.TABLE_NAME} ADD COLUMN ${BaseImageDbo.COLUMN_THUMB_URI} TEXT DEFAULT NULL")
    }
}
