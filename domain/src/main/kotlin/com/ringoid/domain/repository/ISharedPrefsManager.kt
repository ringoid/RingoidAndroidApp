package com.ringoid.domain.repository

import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun accessToken(): AccessToken?

    fun currentUserId(): String?

    fun saveUserProfile(userId: String, accessToken: String)

    fun deleteUserProfile(userId: String)
}
