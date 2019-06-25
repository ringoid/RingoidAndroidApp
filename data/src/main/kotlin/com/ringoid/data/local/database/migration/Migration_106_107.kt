package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_106_107 @Inject constructor() : Migration(106, 107) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE LikedFeedItemIds")
        database.execSQL("DROP TABLE UserMessagedFeedItemIds")
    }
}
