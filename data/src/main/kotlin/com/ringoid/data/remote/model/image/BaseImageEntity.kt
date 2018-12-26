package com.ringoid.data.remote.model.image

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.Mappable

abstract class BaseImageEntity<T>(
    @SerializedName(COLUMN_ID) val id: String,
    @SerializedName(COLUMN_URI) val uri: String) : Mappable<T> {

    companion object {
        const val COLUMN_ID = "photoId"
        const val COLUMN_URI = "photoUri"
    }
}
