package com.ringoid.domain.model.essence.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

/**
 * {
 *   "yearOfBirth":1982,
 *   "sex":"male" // possible values are **male** or **female**,
 *   "dtTC":1535120929, //unix time when Terms and Conditions were accepted
 *   "dtLA":1535120929, //unix time when Privacy Notes were accepted
 *   "dtPN":1535120929, //unix time when Legal age was confirmed
 *   "locale":"en",
 *   "deviceModel":"device model info",
 *   "osVersion":"version of os"
 * }
 */
data class AuthCreateProfileEssence(
    @Expose @SerializedName(COLUMN_BIRTH_YEAR) val yearOfBirth: Int,
    @Expose @SerializedName(COLUMN_DT_LA) val dtLegalAge: Long,
    @Expose @SerializedName(COLUMN_DT_PN) val dtPrivacy: Long = dtLegalAge,
    @Expose @SerializedName(COLUMN_DT_TC) val dtTCs: Long = dtLegalAge,
    @Expose @SerializedName(COLUMN_LOCALE) val locale: String,
    @Expose @SerializedName(COLUMN_SEX) val sex: String,
    @Expose @SerializedName(COLUMN_DEVICE) val device: String,
    @Expose @SerializedName(COLUMN_OS_VERSION) val osVersion: String) : IEssence {

    companion object {
        const val COLUMN_BIRTH_YEAR = "yearOfBirth"
        const val COLUMN_DT_LA = "dtLA"
        const val COLUMN_DT_PN = "dtPN"
        const val COLUMN_DT_TC = "dtTC"
        const val COLUMN_LOCALE = "locale"
        const val COLUMN_SEX = "sex"
        const val COLUMN_DEVICE = "deviceModel"
        const val COLUMN_OS_VERSION = "osVersion"
    }
}
