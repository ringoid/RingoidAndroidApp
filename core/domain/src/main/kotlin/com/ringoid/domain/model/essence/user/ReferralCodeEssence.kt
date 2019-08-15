package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *  "accessToken":"adasdasd-fadfs-sdffd",
 *  "referralId":"masha2001"
 * }
 */
data class ReferralCodeEssence(
    @Expose @SerializedName(COLUMN_ACCESS_TOKEN) val accessToken: String,
    @Expose @SerializedName(COLUMN_REFERRAL_ID) val referralId: String) : IEssence {

    companion object {
        const val COLUMN_ACCESS_TOKEN = "accessToken"
        const val COLUMN_REFERRAL_ID = "referralId"

        fun from(essence: ReferralCodeEssenceUnauthorized, accessToken: String): ReferralCodeEssence =
            ReferralCodeEssence(accessToken = accessToken, referralId = essence.referralId)
    }

    override fun toSentryPayload(): String = "[referralId=$referralId]"
}
