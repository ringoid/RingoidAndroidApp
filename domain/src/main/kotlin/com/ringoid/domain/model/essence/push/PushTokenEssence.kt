package com.ringoid.domain.model.essence.push

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * {
 *  "accessToken":"kjsdfhkjh-asldnl",
 *  "deviceToken":"adasldm;alsk--sad"
 * }
 */
data class PushTokenEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_PUSH_TOKEN) override val pushToken: String) : IPushTokenEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_PUSH_TOKEN = "deviceToken"

        fun from(essence: PushTokenEssenceUnauthorized, accessToken: String): PushTokenEssence =
            PushTokenEssence(accessToken = accessToken, pushToken = essence.pushToken)
    }
}
