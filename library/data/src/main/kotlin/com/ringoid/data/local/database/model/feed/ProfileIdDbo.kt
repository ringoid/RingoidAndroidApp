package com.ringoid.data.local.database.model.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable

@Entity(tableName = ProfileIdDbo.TABLE_NAME)
data class ProfileIdDbo(@PrimaryKey @ColumnInfo(name = COLUMN_ID, index = true) val id: String) : Mappable<String> {

    companion object {
        const val COLUMN_ID = "id"

        const val TABLE_NAME = "ProfileIds"
    }

    override fun map(): String = id
}
