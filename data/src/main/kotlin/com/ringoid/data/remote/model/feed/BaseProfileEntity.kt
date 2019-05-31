package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.Mappable

/**
 * {
 *   "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *   "defaultSortingOrderPosition":0,
 *   "lastOnlineText": "18мин назад",
 *   "lastOnlineFlag": "online",
 *   "distanceText": "unknown",
 *   "photos": [
 *     {
 *       "photoId": "480x640_sfsdfsdfsdf",
 *       "photoUri": "https://bla-bla.jpg"
 *     },
 *     ...
 *   ],
 *   "age": 37,
 *   "property": 0,
 *   "transport": 0,
 *   "education": 0,
 *   "income": 0,
 *   "height": 0,
 *   "hairColor": 0
 * }
 */
abstract class BaseProfileEntity<T>(
    @Expose @SerializedName(COLUMN_ID) val id: String,
    @Expose @SerializedName(COLUMN_DEFAULT_SORT_POSITION) val sortPosition: Int,
    @Expose @SerializedName(COLUMN_DISTANCE_TEXT) val distanceText: String? = null,
    @Expose @SerializedName(COLUMN_IMAGES) val images: List<ImageEntity> = emptyList(),
    @Expose @SerializedName(COLUMN_LAST_ONLINE_STATUS) val lastOnlineStatus: String? = null,
    @Expose @SerializedName(COLUMN_LAST_ONLINE_TEXT) val lastOnlineText: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_AGE) val age: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_EDUCATION) val education: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HAIR_COLOR) val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HEIGHT) val height: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_INCOME) val income: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_PROPERTY) val property: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_TRANSPORT) val transport: Int = DomainUtil.UNKNOWN_VALUE)
    : Mappable<T> {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_DISTANCE_TEXT = "distanceText"
        const val COLUMN_IMAGES = "photos"
        const val COLUMN_LAST_ONLINE_STATUS = "lastOnlineFlag"
        const val COLUMN_LAST_ONLINE_TEXT = "lastOnlineText"
        const val COLUMN_PROPERTY_AGE = "age"
        const val COLUMN_PROPERTY_EDUCATION = "education"
        const val COLUMN_PROPERTY_HAIR_COLOR = "hairColor"
        const val COLUMN_PROPERTY_HEIGHT = "height"
        const val COLUMN_PROPERTY_INCOME = "income"
        const val COLUMN_PROPERTY_PROPERTY = "property"
        const val COLUMN_PROPERTY_TRANSPORT = "transport"
    }

    override fun toString(): String = "BaseProfileEntity(id='$id', sortPosition=$sortPosition, distanceText=$distanceText, images=$images, lastOnlineStatus=$lastOnlineStatus, lastOnlineText=$lastOnlineText, age=$age, education=$education, hairColor=$hairColor, height=$height, income=$income, property=$property, transport=$transport)"
}
