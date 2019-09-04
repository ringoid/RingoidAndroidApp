package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_201_202 @Inject constructor() : BaseMigration(201, 202) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_STATUS_TEXT} TEXT")
    }
}
