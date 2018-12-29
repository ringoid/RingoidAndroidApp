package com.ringoid.data.local.shared_prefs

import android.content.Context
import android.content.SharedPreferences
import com.ringoid.domain.model.user.AccessToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsManager @Inject constructor(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        const val SHARED_PREFS_FILE_NAME = "Ringoid.prefs"

        /* Auth */
        // --------------------------------------
        const val SP_KEY_AUTH_USER_ID = "sp_key_auth_user_id"
        const val SP_KEY_AUTH_ACCESS_TOKEN = "sp_key_auth_access_token"
    }

    /* Auth */
    // --------------------------------------------------------------------------------------------
    fun accessToken(): AccessToken? =
        sharedPreferences
            .takeIf { it.contains(SP_KEY_AUTH_USER_ID) }
            ?.let {
                it.getString(SP_KEY_AUTH_USER_ID, null)
                  ?.let { AccessToken(accessToken = it) }
            }

    fun saveUserProfile(userId: String, accessToken: String) {
        sharedPreferences.edit()
            .putString(SP_KEY_AUTH_USER_ID, userId)
            .putString(SP_KEY_AUTH_ACCESS_TOKEN, accessToken)
            .apply()
    }
}
