package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.image.BaseImageDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_100_101 @Inject constructor() : BaseMigration(100, 101) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${ImageDbo.TABLE_NAME} ADD COLUMN ${BaseImageDbo.COLUMN_THUMB_URI} TEXT DEFAULT NULL")
    }
}
