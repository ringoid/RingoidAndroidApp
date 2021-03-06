package com.ringoid.datainterface.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.datainterface.remote.model.BaseResponse
import com.ringoid.domain.model.Mappable
import com.ringoid.domain.model.feed.Feed

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "profiles":[
 *     {
 *       "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *       "defaultSortingOrderPosition":0,
 *       "photos": [
 *         {
 *           "photoId": "480x640_sfsdfsdfsdf",
 *           "photoUri": "https://bla-bla.jpg"
 *         },
 *         {
 *           "photoId": "480x640_gfgsdfsdf",
 *           "photoUri": "https://bla-bla.jpg"
 *         },
 *         {
 *           "photoId": "480x640_gfdsfsdfsdf",
 *           "photoUri": "https://bla-bla.jpg"
 *         }
 *       ]
 *     },
 *     ...
 *   ]
 * }
 */
class FeedResponse(
    @Expose @SerializedName(COLUMN_PROFILES) val profiles: List<ProfileEntity> = emptyList(),
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec), Mappable<Feed> {

    fun copyWith(profiles: List<ProfileEntity> = this.profiles): FeedResponse =
        FeedResponse(profiles = profiles, errorCode = errorCode, errorMessage = errorMessage)

    companion object {
        const val COLUMN_PROFILES = "profiles"
    }

    override fun map(): Feed = Feed(profiles = profiles.map { it.map() })

    override fun toLogString(): String = "profiles[${profiles.size}]"

    override fun toString(): String = "FeedResponse(profiles=${profiles.joinToString(", ", "[", "]")}, ${super.toString()})"
}
