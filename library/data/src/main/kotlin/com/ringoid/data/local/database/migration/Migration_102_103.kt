package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.datainterface.remote.model.feed.BaseProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_102_103 @Inject constructor() : BaseMigration(102, 103) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_AGE} INTEGER NOT NULL DEFAULT 0")
    }
}
