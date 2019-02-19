package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.domain.model.Mappable

abstract class BaseProfileEntity<T>(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_DEFAULT_SORT_POSITION) val sortPosition: Int,
    @Expose @SerializedName(COLUMN_IMAGES) val images: List<ImageEntity> = emptyList())
    : Mappable<T> {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_IMAGES = "photos"
    }

    override fun toString(): String = "BaseProfileEntity(id='$id', sortPosition=$sortPosition, images=${images.joinToString(", ", "[", "]")})"
}
