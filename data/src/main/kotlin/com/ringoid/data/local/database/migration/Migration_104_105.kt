package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_104_105 @Inject constructor() : BaseMigration(104, 105) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_CHILDREN} INTEGER NOT NULL DEFAULT 0")
    }
}
