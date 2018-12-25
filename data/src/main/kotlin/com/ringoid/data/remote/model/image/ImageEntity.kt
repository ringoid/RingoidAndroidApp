package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.image.Image

/**
 * {
 *   "photoId":"12dd",
 *   "photoUri":"https://bla-bla.com/sss.jpg"
 * }
 */
open class ImageEntity(
    @SerializedName(COLUMN_ID) val id: String,
    @SerializedName(COLUMN_URI) val uri: String) : Mappable<Image> {

    companion object {
        const val COLUMN_ID = "photoId"
        const val COLUMN_URI = "photoUri"
    }

    override fun map(): Image = Image(id = id, uri = uri)
}
