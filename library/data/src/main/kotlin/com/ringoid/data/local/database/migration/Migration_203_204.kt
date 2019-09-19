package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_203_204 @Inject constructor() : BaseMigration(203, 204) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_READ_MESSAGE_ID} TEXT NOT NULL DEFAULT ''")
        execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_READ_MESSAGE_PEER_ID} TEXT NOT NULL DEFAULT ''")
    }
}
