package com.ringoid.datainterface.remote.model.image

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.Mappable

abstract class BaseImageEntity<T>(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_URI) val uri: String,
    @Expose @SerializedName(COLUMN_THUMB_URI) val thumbnailUri: String? = null) : Mappable<T> {

    companion object {
        const val COLUMN_ID = "photoId"
        const val COLUMN_URI = "photoUri"
        const val COLUMN_THUMB_URI = "thumbnailPhotoUri"
    }

    override fun toString(): String = "BaseImageEntity(id='$id', uri='$uri', thumbnailUri='$thumbnailUri')"
}
