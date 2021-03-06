package com.ringoid.origin.style

import androidx.annotation.StyleRes
import com.ringoid.origin.R
import timber.log.Timber

object ThemeUtils {

    private val defaultTheme: ThemeId = ThemeId.DARK

    fun isDefaultTheme(spm: StyleSharedPrefsManager): Boolean = spm.getThemeId(defaultTheme) == defaultTheme
    fun isDarkTheme(spm: StyleSharedPrefsManager): Boolean =
        spm.getThemeId(defaultTheme).let { it == ThemeId.DARK || it == ThemeId.DARK_SEMITRANSPARENT }

    fun printThemes() {
        Timber.v("Dark theme: ${R.style.AppTheme_Dark}")
        Timber.v("Dark semi theme: ${R.style.AppTheme_Dark_SemiTransparent}")
        Timber.v("Light theme: ${R.style.AppTheme_Light}")
        Timber.v("Light semi theme: ${R.style.AppTheme_Light_SemiTransparent}")
    }

    @StyleRes
    fun getThemeById(themeId: ThemeId): Int =
        when (themeId) {
            ThemeId.DARK -> R.style.AppTheme_Dark
            ThemeId.DARK_SEMITRANSPARENT -> R.style.AppTheme_Dark_SemiTransparent
            ThemeId.LIGHT -> R.style.AppTheme_Light
            ThemeId.LIGHT_SEMITRANSPARENT -> R.style.AppTheme_Light_SemiTransparent
            ThemeId.UNKNOWN -> 0
        }

    fun switchTheme(spm: StyleSharedPrefsManager): ThemeId {
        val newTheme = when (spm.getThemeId()) {
            ThemeId.DARK -> ThemeId.LIGHT
            ThemeId.DARK_SEMITRANSPARENT -> ThemeId.LIGHT_SEMITRANSPARENT
            ThemeId.LIGHT -> ThemeId.DARK
            ThemeId.LIGHT_SEMITRANSPARENT -> ThemeId.DARK_SEMITRANSPARENT
            else -> defaultTheme  // default theme
        }
        spm.saveThemeId(theme = newTheme)
        return newTheme
    }
}
