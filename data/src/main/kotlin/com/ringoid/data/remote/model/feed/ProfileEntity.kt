package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.feed.Profile

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
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_DEFAULT_SORT_POSITION) val sortPosition: Int,
    @Expose @SerializedName(COLUMN_IMAGES) val images: List<ImageEntity> = emptyList())
    : Mappable<Profile> {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_IMAGES = "photos"
    }

    override fun map(): Profile = Profile(id = id, images = images.map { it.map() })
}
