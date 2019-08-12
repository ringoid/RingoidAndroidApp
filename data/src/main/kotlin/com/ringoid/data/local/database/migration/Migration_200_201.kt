package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_200_201 @Inject constructor() : Migration(200, 201) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_ABOUT} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_COMPANY} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_JOB_TITLE} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_NAME} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_INSTAGRAM} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_TIKTOK} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_UNIVERSITY} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_WHERE_FROM} TEXT")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_CUSTOM_PROPERTY_WHERE_LIVE} TEXT")
    }
}
