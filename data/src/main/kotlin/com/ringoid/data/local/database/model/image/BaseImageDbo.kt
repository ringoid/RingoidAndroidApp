package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.Image

open class BaseImageDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_URI) val uri: String?) : Mappable<Image> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_URI = "uri"
    }

    override fun map(): Image = Image(id = id, uri = uri)
}
