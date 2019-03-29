package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence
import com.ringoid.domain.model.user.UserSettings
import java.util.*

/**
 * {
 *   "yearOfBirth":1982,
 *   "sex":"male" // possible values are **male** or **female**,
 *   "dtTC":1535120929, //unix time when Terms and Conditions were accepted
 *   "dtLA":1535120929, //unix time when Privacy Notes were accepted
 *   "dtPN":1535120929, //unix time when Legal age was confirmed
 *   "locale":"en",
 *   "deviceModel":"device model info",
 *   "osVersion":"version of os",
 *   "referralId":"masha123",
 *   "privateKey":"ksjdhf9-lsdf-223jd",
 *   "settings":{
 *      "locale":"en",
 *      "push":true,
 *      "timeZone":3
 *   }
 * }
 */
data class AuthCreateProfileEssence(
    @Expose @SerializedName(COLUMN_BIRTH_YEAR) val yearOfBirth: Int,
    @Expose @SerializedName(COLUMN_DT_LA) val dtLegalAge: Long = System.currentTimeMillis(),
    @Expose @SerializedName(COLUMN_DT_PN) val dtPrivacy: Long = dtLegalAge,
    @Expose @SerializedName(COLUMN_DT_TC) val dtTCs: Long = dtLegalAge,
    @Expose @SerializedName(COLUMN_LOCALE) val locale: String = Locale.getDefault().language,
    @Expose @SerializedName(COLUMN_SEX) val sex: String,
    @Expose @SerializedName(COLUMN_DEVICE) val device: String = "",
    @Expose @SerializedName(COLUMN_OS_VERSION) val osVersion: String = "",
    @Expose @SerializedName(COLUMN_PRIVATE_KEY) val privateKey: String? = null,
    @Expose @SerializedName(COLUMN_REFERRAL_ID) val referralId: String? = null,
    @Expose @SerializedName(COLUMN_SETTINGS) val settings: UserSettings? = null) : IEssence {

    companion object {
        const val COLUMN_BIRTH_YEAR = "yearOfBirth"
        const val COLUMN_DT_LA = "dtLA"
        const val COLUMN_DT_PN = "dtPN"
        const val COLUMN_DT_TC = "dtTC"
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_SEX = "sex"
        const val COLUMN_DEVICE = "deviceModel"
        const val COLUMN_OS_VERSION = "osVersion"
        const val COLUMN_PRIVATE_KEY = "privateKey"
        const val COLUMN_REFERRAL_ID = "referralId"
        const val COLUMN_SETTINGS = "settings"
    }

    override fun toSentryPayload(): String = "[yearOfBirth=$yearOfBirth, sex=$sex]"
}
