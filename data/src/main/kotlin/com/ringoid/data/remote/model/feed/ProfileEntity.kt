package com.ringoid.data.remote.model.feed

import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.domain.DomainUtil
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
class ProfileEntity(
    id: String,
    sortPosition: Int = 0,
    distanceText: String? = null,
    images: List<ImageEntity> = emptyList(),
    lastOnlineStatus: String? = null,
    lastOnlineText: String? = null,
    age: Int = DomainUtil.UNKNOWN_VALUE,
    education: Int = DomainUtil.UNKNOWN_VALUE,
    hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    height: Int = DomainUtil.UNKNOWN_VALUE,
    income: Int = DomainUtil.UNKNOWN_VALUE,
    property: Int = DomainUtil.UNKNOWN_VALUE,
    transport: Int = DomainUtil.UNKNOWN_VALUE)
    : BaseProfileEntity<Profile>(
        id = id,
        sortPosition = sortPosition,
        distanceText = distanceText,
        images = images,
        lastOnlineStatus = lastOnlineStatus,
        lastOnlineText = lastOnlineText,
        age = age,
        education = education,
        hairColor = hairColor,
        height = height,
        income = income,
        property = property,
        transport = transport) {

    override fun map(): Profile =
        Profile(
            id = id,
            distanceText = distanceText,
            images = images.map { it.map() },
            lastOnlineStatus = lastOnlineStatus,
            lastOnlineText = lastOnlineText,
            age = age,
            education = education,
            hairColor = hairColor,
            height = height,
            income = income,
            property = property,
            transport = transport)
}
