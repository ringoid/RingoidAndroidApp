package com.ringoid.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

abstract class BaseMigration(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion) {

    protected abstract val migrationBlock: SupportSQLiteDatabase.() -> Unit

    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        try {
            database.migrationBlock()
            database.setTransactionSuccessful()
        } catch (e: Exception) {
            Timber.e(e, "Can't migrate")
            throw IllegalStateException(e)
        } finally {
            database.endTransaction()
        }
    }
}
