package com.ringoid.domain.manager

import androidx.annotation.StyleRes
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.GpsLocation
import com.ringoid.domain.misc.UserProfilePropertiesRaw
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
    fun enableDebugLog(isEnabled: Boolean)

    @DebugOnly
    fun switchDebugLogEnabled()

    @DebugOnly
    fun testBackup()

    fun isDeveloperModeEnabled(): Boolean

    fun enableDeveloperMode()

    fun switchDeveloperMode()

    /* Auth */
    // ------------------------------------------
    fun accessToken(): AccessToken?

    fun currentUserId(): String?
    fun currentUserCreateTs(): Long
    fun currentUserGender(): Gender
    fun currentUserYearOfBirth(): Int

    fun hasUserCreateTs(): Boolean
    fun hasUserGender(): Boolean
    fun hasUserYearOfBirth(): Boolean

    fun saveUserProfile(userId: String, userGender: Gender, userYearOfBirth: Int, accessToken: String)
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
    fun getUserSettingDailyPushEnabled(): Boolean
    fun getUserSettingLikesPushEnabled(): Boolean
    fun getUserSettingMatchesPushEnabled(): Boolean
    fun getUserSettingMessagesPushEnabled(): Boolean

    fun setUserSettingDailyPushEnabled(pushEnabled: Boolean)
    fun setUserSettingLikesPushEnabled(pushEnabled: Boolean)
    fun setUserSettingMatchesPushEnabled(pushEnabled: Boolean)
    fun setUserSettingMessagesPushEnabled(pushEnabled: Boolean)

    fun getUserProfileProperties(): UserProfilePropertiesRaw
    fun setUserProfileProperties(propertiesRaw: UserProfilePropertiesRaw)
    fun dropUserProfileProperties()

    // ------------------------------------------
    fun getBigEditText(): String
    fun setBigEditText(text: String)
    fun dropBigEditText()
}
