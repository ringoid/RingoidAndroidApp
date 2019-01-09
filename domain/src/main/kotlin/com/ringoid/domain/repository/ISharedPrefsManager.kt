package com.ringoid.domain.repository

import com.ringoid.domain.model.user.AccessToken

interface ISharedPrefsManager {

    fun accessToken(): AccessToken?

    fun saveUserProfile(userId: String, accessToken: String)
}
