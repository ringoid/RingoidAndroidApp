package com.ringoid.domain.manager

import androidx.annotation.StyleRes
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun getAppUid(): String

    // ------------------------------------------
    fun getByKey(key: String): String?

    fun saveByKey(key: String, json: String)

    fun deleteByKey(key: String)

    // ------------------------------------------
    fun getThemeResId(@StyleRes defaultThemeResId: Int = 0): Int

    fun saveThemeResId(@StyleRes themeResId: Int)

    // ------------------------------------------
    @DebugOnly
    fun isDebugLogEnabled(): Boolean

    @DebugOnly
    fun switchDebugLogEnabled()

    @DebugOnly
    fun testBackup()

    /* Auth */
    // ------------------------------------------
    fun accessToken(): AccessToken?

    fun currentUserId(): String?

    fun saveUserProfile(userId: String, accessToken: String)

    fun deleteUserProfile(userId: String)

    /* Location */
    // ------------------------------------------
    fun getLocation(): GpsLocation?

    fun saveLocation(location: GpsLocation)

    fun deleteLocation()

    /* Referral program */
    // ------------------------------------------
    fun createPrivateKeyIfNotExists(): String?

    fun getPrivateKey(): String?

    fun hasPrivateKey(): Boolean

    fun setPrivateKey(privateKey: String?)

    fun getReferralCode(): String?

    fun hasReferralCode(): Boolean

    fun setReferralCode(code: String?)

    /* User Settings */
    // ------------------------------------------
    fun getUserSettingPushEnabled(): Boolean

    fun setUserSettingPushEnabled(pushEnabled: Boolean)
}
