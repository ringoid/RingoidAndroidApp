package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.BaseImageEntity
import com.ringoid.data.remote.model.messenger.MessageEntity

/**
 * {
 *   "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *   "defaultSortingOrderPosition":0,
 *   "notSeen":true,
 *   "messages":[...],
 *   "photos": [
 *     {
 *       "photoId": "480x640_sfsdfsdfsdf",
 *       "photoUri": "https://bla-bla.jpg"
 *     },
 *     ...
 *   ]
 * }
 */
class FeedItemEntity(
    @SerializedName(COLUMN_FLAG_NOT_SEEN) val notSeen: Boolean,
    @SerializedName(COLUMN_MESSAGES) val messages: List<MessageEntity> = emptyList(),
    id: String, sortPosition: Int, images: List<BaseImageEntity> = emptyList())
    : ProfileEntity(id = id, sortPosition = sortPosition, images = images) {

    companion object {
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_MESSAGES = "messages"
    }
}
