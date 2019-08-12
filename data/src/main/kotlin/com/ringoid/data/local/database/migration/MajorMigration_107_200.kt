package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.messenger.MessageDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MajorMigration_107_200 @Inject constructor() : BaseMigration(107, 200) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("DELETE FROM ${MessageDbo.TABLE_NAME}")
    }
}
