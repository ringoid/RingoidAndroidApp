package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

open class BaseImageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_URI) val uri: String?,
    @ColumnInfo(name = COLUMN_THUMB_URI) val thumbnailUri: String? = null) {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_URI = "uri"
        const val COLUMN_THUMB_URI = "thumbnailPhotoUri"
    }
}
