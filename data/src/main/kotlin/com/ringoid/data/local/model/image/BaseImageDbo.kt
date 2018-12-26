package com.ringoid.data.local.model.image

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

open class BaseImageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_URI) val uri: String) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_URI = "uri"
    }
}
