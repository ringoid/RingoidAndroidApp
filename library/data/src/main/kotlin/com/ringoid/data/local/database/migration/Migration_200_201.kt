package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_200_201 @Inject constructor() : BaseMigration(200, 201) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_ABOUT} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_COMPANY} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_JOB_TITLE} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_NAME} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_INSTAGRAM} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_TIKTOK} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_UNIVERSITY} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_WHERE_FROM} TEXT")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_WHERE_LIVE} TEXT")
    }
}
