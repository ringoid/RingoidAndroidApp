package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence

@Entity(tableName = ImageRequestDbo.TABLE_NAME)
data class ImageRequestDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,  // 0 means 'not set'
    @ColumnInfo(name = COLUMN_ACCESS_TOKEN) val accessToken: String = "",
    @ColumnInfo(name = COLUMN_CLIENT_IMAGE_ID) val clientImageId: String = DomainUtil.BAD_ID,
    @ColumnInfo(name = COLUMN_ORIGIN_IMAGE_ID) val originImageId: String = DomainUtil.BAD_ID,
    @ColumnInfo(name = COLUMN_EXTENSION) val extension: String = "",
    @ColumnInfo(name = COLUMN_TYPE) val type: String) {

    fun createRequestEssence(): ImageUploadUrlEssence =
        ImageUploadUrlEssence(accessToken = accessToken, clientImageId = clientImageId, extension = extension)

    fun deleteRequestEssence(): ImageDeleteEssence =
        ImageDeleteEssence(accessToken = accessToken, imageId = originImageId)

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_CLIENT_IMAGE_ID = "clientImageId"  // for create only
        const val COLUMN_ORIGIN_IMAGE_ID = "originImageId"  // for delete only
        const val COLUMN_EXTENSION = "extension"  // for create only
        const val COLUMN_TYPE = "type"  // create, delete

        const val TABLE_NAME = "ImageRequests.db"

        const val TYPE_CREATE = "create"
        const val TYPE_DELETE = "delete"

        fun from(essence: ImageUploadUrlEssence): ImageRequestDbo =
            ImageRequestDbo(accessToken = essence.accessToken, clientImageId = essence.clientImageId, extension = essence.extension, type = TYPE_CREATE)

        fun from(essence: ImageDeleteEssence): ImageRequestDbo =
            ImageRequestDbo(accessToken = essence.accessToken, originImageId = essence.imageId, type = TYPE_DELETE)
    }
}
