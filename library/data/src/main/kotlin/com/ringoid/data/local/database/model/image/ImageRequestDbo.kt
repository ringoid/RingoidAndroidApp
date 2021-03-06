package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.ImageRequest

@Entity(tableName = ImageRequestDbo.TABLE_NAME)
data class ImageRequestDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,  // 0 means 'not set'
    @ColumnInfo(name = COLUMN_ACCESS_TOKEN) val accessToken: String = "",
    @ColumnInfo(name = COLUMN_CLIENT_IMAGE_ID) val clientImageId: String = DomainUtil.BAD_ID,
    @ColumnInfo(name = COLUMN_ORIGIN_IMAGE_ID) val originImageId: String = DomainUtil.BAD_ID,
    @ColumnInfo(name = COLUMN_EXTENSION) val extension: String = "",
    @ColumnInfo(name = COLUMN_IMAGE_FILE_PATH) val imageFilePath: String = "",
    @ColumnInfo(name = COLUMN_TYPE) val type: String): Mappable<ImageRequest> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_CLIENT_IMAGE_ID = "clientImageId"  // for create only
        const val COLUMN_ORIGIN_IMAGE_ID = "originImageId"  // for delete only
        const val COLUMN_EXTENSION = "extension"  // for create only
        const val COLUMN_IMAGE_FILE_PATH = "imageFilePath"  // for create only
        const val COLUMN_TYPE = "type"  // create, delete

        const val TABLE_NAME = "ImageRequests"

        fun from(request: ImageRequest): ImageRequestDbo =
            ImageRequestDbo(id = request.id, accessToken = request.accessToken,
                            clientImageId = request.clientImageId, originImageId = request.originImageId,
                            extension = request.extension, imageFilePath = request.imageFilePath,
                            type = request.type)

        fun from(essence: ImageUploadUrlEssence, imageFilePath: String): ImageRequestDbo =
            ImageRequestDbo(accessToken = essence.accessToken, clientImageId = essence.clientImageId,
                            extension = essence.extension, imageFilePath = imageFilePath,
                            type = ImageRequest.TYPE_CREATE)

        fun from(essence: ImageDeleteEssence): ImageRequestDbo =
            ImageRequestDbo(accessToken = essence.accessToken, originImageId = essence.imageId, type = ImageRequest.TYPE_DELETE)
    }

    override fun map(): ImageRequest =
        ImageRequest(id = id, accessToken = accessToken, clientImageId = clientImageId,
                     originImageId = originImageId, extension = extension,
                     imageFilePath = imageFilePath, type = type)
}
