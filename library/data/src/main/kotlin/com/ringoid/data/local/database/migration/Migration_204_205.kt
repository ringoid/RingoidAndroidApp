package com.ringoid.data.local.database.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import com.ringoid.domain.DomainUtil
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration_204_205 @Inject constructor() : BaseMigration(204, 205) {

    override val migrationBlock: SupportSQLiteDatabase.() -> Unit = {
        execSQL("ALTER TABLE ${ActionObjectDbo.TABLE_NAME} ADD COLUMN ${ActionObjectDbo.COLUMN_ACTION_ID} INTEGER NOT NULL DEFAULT ${DomainUtil.UNKNOWN_VALUE}")
    }
}
