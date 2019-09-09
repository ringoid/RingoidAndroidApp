package com.ringoid.domain.manager

import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.misc.*
import com.ringoid.domain.model.user.AccessToken
import com.ringoid.utility.DebugOnly

interface ISharedPrefsManager : IFiltersSource {

    fun getAppUid(): String
    fun isFirstAppLaunch(): Boolean
    fun dropFirstAppLaunch()

    // ------------------------------------------
    fun getByKey(key: String): String?
    fun saveByKey(key: String, json: String)
    fun deleteByKey(key: String)

    // ------------------------------------------
    @DebugOnly fun isDebugLogEnabled(): Boolean
    @DebugOnly fun enableDebugLog(isEnabled: Boolean)
    @DebugOnly fun switchDebugLogEnabled()

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

    fun onLogout()

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
    fun setReferralCode(code: String?, dontOverride: Boolean = false)

    /* User Settings */
    // ------------------------------------------
    fun getUserPushSettings(): PushSettingsRaw
    fun setUserPushSettings(settingsRaw: PushSettingsRaw)
    fun dropUserPushSettings()

    fun getUserProfileProperties(): UserProfilePropertiesRaw
    fun setUserProfileProperties(propertiesRaw: UserProfilePropertiesRaw)
    fun dropUserProfileProperties()

    fun getUserProfileCustomPropertiesUnsavedInput(): UserProfileCustomPropertiesUnsavedInput
    fun setUserProfileCustomPropertiesUnsavedInput(unsavedInput: UserProfileCustomPropertiesUnsavedInput)
    fun dropUserProfileCustomPropertiesUnsavedInput()

    // ------------------------------------------
    fun getBigEditText(): String
    fun setBigEditText(text: String)
    fun dropBigEditText()

    // ------------------------------------------
    fun needShowFilters(): Boolean
    fun needShowFiltersOnLc(): Boolean
    fun getNeedShowStubStatus(): Boolean
    fun dropNeedShowStubStatus()
}
