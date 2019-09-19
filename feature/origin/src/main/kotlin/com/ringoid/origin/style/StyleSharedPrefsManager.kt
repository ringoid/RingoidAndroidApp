package com.ringoid.origin.style

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSharedPrefsManager @Inject constructor(context: Context) {

    companion object {
        private const val SHARED_PREFS_FILE_NAME = "RingoidStyle.prefs"

        private const val SP_KEY_THEME = "sp_key_theme"
    }

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeId(defaultTheme: ThemeId = ThemeId.DARK): ThemeId =
        sharedPreferences.getString(SP_KEY_THEME, defaultTheme.name)
            ?.let { ThemeId.valueOf(it) }
            ?: ThemeId.valueOf(defaultTheme.name)

    internal fun saveThemeId(theme: ThemeId) {
        sharedPreferences.edit().putString(SP_KEY_THEME, theme.name).apply()
    }
}
