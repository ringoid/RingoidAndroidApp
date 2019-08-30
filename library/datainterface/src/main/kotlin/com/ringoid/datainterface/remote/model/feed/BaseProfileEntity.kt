package com.ringoid.datainterface.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.datainterface.remote.model.image.ImageEntity
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
 *   "sex": "male",
 *   "property": 0,
 *   "transport": 0,
 *   "educationLevel": 0,
 *   "income": 0,
 *   "height": 0,
 *   "hairColor": 0,
 *   "children":0,
 *
 *   "name":"Mikhail",
 *   "jobTitle":"Developer",
 *   "company":"Ringoid",
 *   "education":"BGTU Voenmeh",
 *   "about":"Nice person",
 *   "instagram":"unknown",
 *   "tikTok":"unknown",
 *   "statusText":"This is my status!",
 *   "whereLive":"St.Petersburg",
 *   "whereFrom":"Leningrad"
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
    @Expose @SerializedName(COLUMN_PROPERTY_CHILDREN) val children: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_EDUCATION) val education: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_GENDER) val gender: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_HAIR_COLOR) val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_HEIGHT) val height: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_INCOME) val income: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_PROPERTY) val property: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_TRANSPORT) val transport: Int = DomainUtil.UNKNOWN_VALUE,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_ABOUT) val about: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_COMPANY) val company: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_JOB_TITLE) val jobTitle: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_NAME) val name: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM) val instagram: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK) val tiktok: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_STATUS_TEXT) val status: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_UNIVERSITY) val university: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_FROM) val whereFrom: String? = null,
    @Expose @SerializedName(COLUMN_PROPERTY_CUSTOM_WHERE_LIVE) val whereLive: String? = null)
    : Mappable<T> {

    companion object {
        const val COLUMN_ID = "userId"
        const val COLUMN_DEFAULT_SORT_POSITION = "defaultSortingOrderPosition"
        const val COLUMN_DISTANCE_TEXT = "distanceText"
        const val COLUMN_IMAGES = "photos"
        const val COLUMN_LAST_ONLINE_STATUS = "lastOnlineFlag"
        const val COLUMN_LAST_ONLINE_TEXT = "lastOnlineText"
        const val COLUMN_PROPERTY_AGE = "age"
        const val COLUMN_PROPERTY_CHILDREN = "children"
        const val COLUMN_PROPERTY_EDUCATION = "educationLevel"
        const val COLUMN_PROPERTY_GENDER = "sex"
        const val COLUMN_PROPERTY_HAIR_COLOR = "hairColor"
        const val COLUMN_PROPERTY_HEIGHT = "height"
        const val COLUMN_PROPERTY_INCOME = "income"
        const val COLUMN_PROPERTY_PROPERTY = "property"
        const val COLUMN_PROPERTY_TRANSPORT = "transport"
        const val COLUMN_PROPERTY_CUSTOM_ABOUT = "about"
        const val COLUMN_PROPERTY_CUSTOM_COMPANY = "company"
        const val COLUMN_PROPERTY_CUSTOM_JOB_TITLE = "jobTitle"
        const val COLUMN_PROPERTY_CUSTOM_NAME = "name"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_INSTAGRAM = "instagram"
        const val COLUMN_PROPERTY_CUSTOM_SOCIAL_TIKTOK = "tikTok"
        const val COLUMN_PROPERTY_CUSTOM_STATUS_TEXT = "statusText"
        const val COLUMN_PROPERTY_CUSTOM_UNIVERSITY = "education"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_FROM = "whereFrom"
        const val COLUMN_PROPERTY_CUSTOM_WHERE_LIVE = "whereLive"
    }

    override fun toString(): String = "BaseProfileEntity(id='$id', sortPosition=$sortPosition, distanceText=$distanceText, images=$images, lastOnlineStatus=$lastOnlineStatus, lastOnlineText=$lastOnlineText, age=$age, children=$children, education=$education, gender=$gender, hairColor=$hairColor, height=$height, income=$income, property=$property, transport=$transport, about=$about, company=$company, jobTitle=$jobTitle, name=$name, instagram=$instagram, tiktok=$tiktok, status=$status, university=$university, whereFrom=$whereFrom, whereLive=$whereLive)"
}
