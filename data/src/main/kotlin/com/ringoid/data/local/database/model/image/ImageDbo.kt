package com.ringoid.data.local.database.model.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.image.Image

@Entity(tableName = ImageDbo.TABLE_NAME,
        foreignKeys = [ForeignKey(entity = FeedItemDbo::class, parentColumns = [FeedItemDbo.COLUMN_ID],
                                  childColumns = [ImageDbo.COLUMN_PROFILE_ID],
                                  onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE)])
class ImageDbo(
    @ColumnInfo(name = COLUMN_PROFILE_ID, index = true) val profileId: String,
    id: String, uri: String, thumbnailUri: String? = null)
    : BaseImageDbo(id = id, uri = uri, thumbnailUri = thumbnailUri), Mappable<Image> {

    companion object {
        const val COLUMN_PROFILE_ID = "profileId"

        const val TABLE_NAME = "Images"

        fun from(image: IImage, profileId: String): ImageDbo =
            ImageDbo(profileId = profileId, id = image.id, uri = image.uri.orEmpty(), thumbnailUri = image.thumbnailUri.orEmpty())
    }

    override fun map(): Image = Image(id = id, uri = uri, thumbnailUri = thumbnailUri)
}
