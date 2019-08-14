package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_103_104 @Inject constructor() : BaseMigration(103, 104) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_EDUCATION} INTEGER NOT NULL DEFAULT 0")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_GENDER} TEXT NOT NULL DEFAULT ''")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_HAIR_COLOR} INTEGER NOT NULL DEFAULT 0")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_HEIGHT} INTEGER NOT NULL DEFAULT 0")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_INCOME} INTEGER NOT NULL DEFAULT 0")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_PROPERTY} INTEGER NOT NULL DEFAULT 0")
        execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${FeedItemDbo.COLUMN_PROPERTY_TRANSPORT} INTEGER NOT NULL DEFAULT 0")
    }
}
