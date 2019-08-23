package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.image.ImageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MajorMigration_10_100 @Inject constructor() : BaseMigration(10, 100) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("DROP TABLE IF EXISTS ${ImageDbo.TABLE_NAME}")
    }
}
