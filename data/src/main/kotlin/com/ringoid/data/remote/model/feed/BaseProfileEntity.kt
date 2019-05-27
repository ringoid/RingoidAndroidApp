package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.domain.model.Mappable

abstract class BaseProfileEntity<T>(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_AGE) val age: Int,
    @Expose @SerializedName(COLUMN_DEFAULT_SORT_POSITION) val sortPosition: Int,
    @Expose @SerializedName(COLUMN_DISTANCE_TEXT) val distanceText: String? = null,
    @Expose @SerializedName(COLUMN_IMAGES) val images: List<ImageEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_LAST_ONLINE_STATUS) val lastOnlineStatus: String? = null,
    @Expose @SerializedName(COLUMN_LAST_ONLINE_TEXT) val lastOnlineText: String? = null)
    : Mappable<T> {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_AGE = "age"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_DISTANCE_TEXT = "distanceText"
        const val COLUMN_IMAGES = "photos"
        const val COLUMN_LAST_ONLINE_STATUS = "lastOnlineFlag"
        const val COLUMN_LAST_ONLINE_TEXT = "lastOnlineText"
    }

    override fun toString(): String = "BaseProfileEntity(id='$id', age=$age, sortPosition=$sortPosition, distanceText='$distanceText', images=${images.joinToString(", ", "[", "]")}, lastOnlineStatus='$lastOnlineStatus', lastOnlineText='$lastOnlineText')"
}
