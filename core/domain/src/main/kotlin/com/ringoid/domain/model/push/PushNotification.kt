package com.ringoid.domain.model.push

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PushNotification(@Expose @SerializedName(COLUMN_MAIN) val content: PushNotificationContent) {

    companion object {
        const val COLUMN_MAIN = "message"

        fun fromJson(json: String): PushNotification = Gson().fromJson(json, PushNotification::class.java)
    }
}

data class PushNotificationContent(
    @Expose @SerializedName(COLUMN_DATA) val data: PushNotificationData? = null,
    @Expose @SerializedName(COLUMN_NOTIFICATION) val body: PushNotificationBody? = null,
    @Expose @SerializedName(COLUMN_TOKEN) val token: String) {

    companion object {
        const val COLUMN_DATA = "data"
        const val COLUMN_NOTIFICATION = "notification"
        const val COLUMN_TOKEN = "token"
    }
}

data class PushNotificationBody(
    @Expose @SerializedName(COLUMN_BODY) val body: String,
    @Expose @SerializedName(COLUMN_TITLE) val title: String) {

    companion object {
        const val COLUMN_BODY = "body"
        const val COLUMN_TITLE = "title"
    }
}

data class PushNotificationData(@Expose @SerializedName(COLUMN_TYPE) val type: String) {

    companion object {
        const val COLUMN_TYPE = "type"

        const val TYPE_LIKE = "NEW_LIKE_PUSH_TYPE"
        const val TYPE_MATCH = "NEW_MATCH_PUSH_TYPE"
        const val TYPE_MESSAGE = "NEW_MESSAGE_PUSH_TYPE"

        fun fromJson(json: String): PushNotificationData = Gson().fromJson(json, PushNotificationData::class.java)
    }
}
