package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.remote.model.feed.BaseProfileEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_103_104 @Inject constructor() : Migration(103, 104) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_EDUCATION} INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_HAIR_COLOR} INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_HEIGHT} INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_INCOME} INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_PROPERTY} INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE ${FeedItemDbo.TABLE_NAME} ADD COLUMN ${BaseProfileEntity.COLUMN_PROPERTY_TRANSPORT} INTEGER NOT NULL DEFAULT 0")

    }
}
