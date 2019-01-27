package com.ringoid.domain.repository

import androidx.annotation.StyleRes
import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun getLoginThemeResId(@StyleRes defaultThemeResId: Int): Int
    fun getMainThemeResId(@StyleRes defaultThemeResId: Int): Int

    fun saveLoginThemeResId(@StyleRes themeResId: Int)
    fun saveMainThemeResId(@StyleRes themeResId: Int)

    // ------------------------------------------
    fun accessToken(): AccessToken?

    fun currentUserId(): String?

    fun saveUserProfile(userId: String, accessToken: String)

    fun deleteUserProfile(userId: String)
}
