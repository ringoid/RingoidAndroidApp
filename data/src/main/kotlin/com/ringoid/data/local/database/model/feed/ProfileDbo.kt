package com.ringoid.data.local.database.model.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = ProfileDbo.TABLE_NAME)
data class ProfileDbo(@PrimaryKey @ColumnInfo(name = COLUMN_ID, index = true) val id: String) {

    companion object {
        const val COLUMN_ID = "id"

        const val TABLE_NAME = "Profiles"
    }
}
