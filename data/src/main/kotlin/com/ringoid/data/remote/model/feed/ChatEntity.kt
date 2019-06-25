package com.ringoid.data.remote.model.feed

import com.ringoid.data.remote.model.image.ImageEntity
import com.ringoid.data.remote.model.messenger.MessageEntity
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Chat

/**
 * {
 *   "userId": "5bdc880d91d60b28b17ab2e58bf4a7c6ab83091e",
 *   "defaultSortingOrderPosition": 0,
 *   "lastOnlineText": "18мин назад",
 *   "lastOnlineFlag": "online",
 *   "distanceText": "unknown",
 *   "notSeen": false,
 *   "photos": [
 *     {
 *       "photoId": "480x640_f34fe06440021c07b4dd3ad77e12475a0cb3640f",
 *       "photoUri": "https://s3-eu-west-1.amazonaws.com/test-ringoid-public-photo/f34fe06440021c07b4dd3ad77e12475a0cb3640f_480x640.jpg",
 *       "thumbnailPhotoUri": "https://s3-eu-west-1.amazonaws.com/test-ringoid-public-photo/f34fe06440021c07b4dd3ad77e12475a0cb3640f_thumbnail.jpg"
 *     }
 *   ],
 *   "messages": [
 *     {
 *       "wasYouSender": false,
 *       "text": "Hello!"
 *     }
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
 *  }
 */
class ChatEntity(
    id: String,
    sortPosition: Int,
    distanceText: String? = null,
    images: List<ImageEntity> = emptyList(),
    messages: List<MessageEntity> = emptyList(),
    isNotSeen: Boolean,
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
    transport: Int = DomainUtil.UNKNOWN_VALUE)
    : FeedItemEntity(
        id = id,
        sortPosition = sortPosition,
        distanceText = distanceText,
        images = images,
        messages = messages,
        isNotSeen = isNotSeen,
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
        transport = transport) {

    fun mapToChat(): Chat = Chat(feedItem = map())
}
