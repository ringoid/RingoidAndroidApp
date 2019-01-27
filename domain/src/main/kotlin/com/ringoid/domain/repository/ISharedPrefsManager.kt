package com.ringoid.domain.repository

import androidx.annotation.StyleRes
import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun getThemeResId(): Int

    fun saveThemeResId(@StyleRes themeResId: Int)

    // ------------------------------------------
    fun accessToken(): AccessToken?

    fun currentUserId(): String?

    fun saveUserProfile(userId: String, accessToken: String)

    fun deleteUserProfile(userId: String)
}
