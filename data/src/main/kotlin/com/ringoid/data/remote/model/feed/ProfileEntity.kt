package com.ringoid.data.remote.model.feed

import com.ringoid.data.remote.model.image.ImageEntity
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
class ProfileEntity(id: String, sortPosition: Int, images: List<ImageEntity> = emptyList())
    : BaseProfileEntity<Profile>(id = id, sortPosition = sortPosition, images = images) {

    override fun map(): Profile = Profile(id = id, images = images.map { it.map() })
}
