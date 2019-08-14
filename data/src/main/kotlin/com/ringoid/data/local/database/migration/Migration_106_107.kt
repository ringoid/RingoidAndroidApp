package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_106_107 @Inject constructor() : BaseMigration(106, 107) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("DROP TABLE LikedFeedItemIds")
        execSQL("DROP TABLE UserMessagedFeedItemIds")
    }
}
