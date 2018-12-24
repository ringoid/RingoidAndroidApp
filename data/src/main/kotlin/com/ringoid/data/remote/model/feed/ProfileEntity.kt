package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.BaseImageEntity

/**
 * {
 *   "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *   "defaultSortingOrderPosition":0,
 *   "photos": [
 *     {
 *       "photoId": "480x640_sfsdfsdfsdf",
 *       "photoUri": "https://bla-bla.jpg"
 *     },
 *     ...
 *   ]
 * }
 */
open class ProfileEntity(
    @SerializedName(COLUMN_ID) val id: String,
    @SerializedName(COLUMN_DEFAULT_SORT_POSITION) val sortPosition: Int,
    @SerializedName(COLUMN_IMAGES) val images: List<BaseImageEntity> = emptyList()) {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_IMAGES = "photos"
    }
}
