package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.image.ImageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MajorMigration_10_100 @Inject constructor() : Migration(10, 100) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS ${ImageDbo.TABLE_NAME}")
    }
}
