package com.ringoid.datainterface.remote.model.feed

import com.ringoid.datainterface.remote.model.image.ImageEntity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.feed.Profile

/**
 * {
 *   "userId":"9091127b2a88b002fad4ef55beb0264222c1ebb7",
 *   "defaultSortingOrderPosition":0,
 *   "totalLikes":0,
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
 *   "education": 0,
 *   "income": 0,
 *   "height": 0,
 *   "hairColor": 0,
 *   "children":0
 * }
 */
class ProfileEntity(
    id: String,
    sortPosition: Int = 0,
    distanceText: String? = null,
    totalLikes: Int = DomainUtil.UNKNOWN_VALUE,
    images: List<ImageEntity> = emptyList(),
    lastOnlineStatus: String? = null,
    lastOnlineText: String? = null,
    age: Int = DomainUtil.UNKNOWN_VALUE,
    children: Int = DomainUtil.UNKNOWN_VALUE,
    education: Int = DomainUtil.UNKNOWN_VALUE,
    gender: String? = null,
    hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    height: Int = DomainUtil.UNKNOWN_VALUE,
    income: Int = DomainUtil.UNKNOWN_VALUE,
    property: Int = DomainUtil.UNKNOWN_VALUE,
    transport: Int = DomainUtil.UNKNOWN_VALUE,
    about: String? = null,
    company: String? = null,
    jobTitle: String? = null,
    name: String? = null,
    instagram: String? = null,
    tiktok: String? = null,
    status: String? = null,
    university: String? = null,
    whereFrom: String? = null,
    whereLive: String? = null)
    : BaseProfileEntity<Profile>(
        id = id,
        sortPosition = sortPosition,
        distanceText = distanceText,
        totalLikes = totalLikes,
        images = images,
        lastOnlineStatus = lastOnlineStatus,
        lastOnlineText = lastOnlineText,
        age = age,
        children = children,
        education = education,
        gender = gender,
        hairColor = hairColor,
        height = height,
        income = income,
        property = property,
        transport = transport,
        about = about,
        company = company,
        jobTitle = jobTitle,
        name = name,
        instagram = instagram,
        tiktok = tiktok,
        status = status,
        university = university,
        whereFrom = whereFrom,
        whereLive = whereLive) {

    override fun map(): Profile =
        Profile(
            id = id,
            distanceText = distanceText,
            totalLikes = totalLikes,
            images = images.map { it.map() },
            lastOnlineStatus = lastOnlineStatus,
            lastOnlineText = lastOnlineText,
            age = age,
            children = children,
            education = education,
            gender = Gender.from(gender),
            hairColor = hairColor,
            height = height,
            income = income,
            property = property,
            transport = transport,
            about = about,
            company = company,
            jobTitle = jobTitle,
            name = name,
            instagram = instagram,
            tiktok = tiktok,
            status = status,
            university = university,
            whereFrom = whereFrom,
            whereLive = whereLive)
}
