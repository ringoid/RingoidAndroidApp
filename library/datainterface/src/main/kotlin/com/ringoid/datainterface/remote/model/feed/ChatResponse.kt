package com.ringoid.data.remote.model.feed

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.data.remote.model.BaseResponse

/**
 * {
 *   "errorCode":"",
 *   "errorMessage":"",
 *   "repeatRequestAfter":0,
 *   "chatExists": true,
 *   "pullAgainAfter": 3000
 *   "chat": {
 *       "userId": "5bdc880d91d60b28b17ab2e58bf4a7c6ab83091e",
 *       "defaultSortingOrderPosition": 0,
 *       "lastOnlineText": "18мин назад",
 *       "lastOnlineFlag": "online",
 *       "distanceText": "unknown",
 *       "notSeen": false,
 *       "photos": [
 *         {
 *           "photoId": "480x640_f34fe06440021c07b4dd3ad77e12475a0cb3640f",
 *           "photoUri": "https://s3-eu-west-1.amazonaws.com/test-ringoid-public-photo/f34fe06440021c07b4dd3ad77e12475a0cb3640f_480x640.jpg",
 *           "thumbnailPhotoUri": "https://s3-eu-west-1.amazonaws.com/test-ringoid-public-photo/f34fe06440021c07b4dd3ad77e12475a0cb3640f_thumbnail.jpg"
 *         }
 *       ],
 *       "messages": [
 *         {
 *           "wasYouSender": false,
 *           "text": "Hello!"
 *         }
 *       ],
 *       "age": 37,
 *       "property": 0,
 *       "transport": 0,
 *       "income": 0,
 *       "height": 0
 *   }
 * }
 */
class ChatResponse(
    @Expose @SerializedName(COLUMN_CHAT) val chat: ChatEntity,
    @Expose @SerializedName(COLUMN_FLAG_CHAT_EXIST) val isChatExist: Boolean = true,
    @Expose @SerializedName(COLUMN_PULL_AFTER) val pullAgainAfter: Long = 0L,
    errorCode: String = "", errorMessage: String = "", repeatAfterSec: Long = 0L)
    : BaseResponse(errorCode, errorMessage, repeatAfterSec) {

    companion object {
        const val COLUMN_CHAT = "chat"
        const val COLUMN_FLAG_CHAT_EXIST = "chatExists"
        const val COLUMN_PULL_AFTER = "pullAgainAfter"
    }
}
