package com.ringoid.domain.repository

import androidx.annotation.StyleRes
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun getThemeResId(@StyleRes defaultThemeResId: Int = 0): Int

    fun saveThemeResId(@StyleRes themeResId: Int)

    // ------------------------------------------
    @DebugOnly
    fun isDebugLogEnabled(): Boolean

    @DebugOnly
    fun switchDebugLogEnabled()

    /* Auth */
    // ------------------------------------------
    fun accessToken(): AccessToken?

    fun currentUserId(): String?

    fun saveUserProfile(userId: String, accessToken: String)

    fun deleteUserProfile(userId: String)

    /* Referral program */
    // ------------------------------------------
    fun createPrivateKeyIfNotExists(): String?

    fun getPrivateKey(): String?

    fun hasPrivateKey(): Boolean

    fun setPrivateKey(privateKey: String?)

    fun getReferralCode(): String?

    fun hasReferralCode(): Boolean

    fun setReferralCode(code: String?)
}
