package com.ringoid.origin.style

import androidx.annotation.StyleRes
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.origin.R
import timber.log.Timber

object ThemeUtils {

    private val defaultTheme = R.style.AppTheme_Dark

    fun isDefaultTheme(spm: ISharedPrefsManager): Boolean = spm.getThemeResId(defaultTheme) == defaultTheme
    fun isDarkTheme(spm: ISharedPrefsManager): Boolean = spm.getThemeResId(defaultTheme) == R.style.AppTheme_Dark

    fun printThemes() {
        Timber.v("Dark theme: ${R.style.AppTheme_Dark}")
        Timber.v("Dark semi theme: ${R.style.AppTheme_Dark_SemiTransparent}")
        Timber.v("Light theme: ${R.style.AppTheme_Light}")
        Timber.v("Light semi theme: ${R.style.AppTheme_Light_SemiTransparent}")
    }

    @StyleRes
    fun switchTheme(spm: ISharedPrefsManager): Int {
        val currentTheme = spm.getThemeResId()
        val newTheme = when (currentTheme) {
            R.style.AppTheme_Dark -> R.style.AppTheme_Light
            R.style.AppTheme_Light -> R.style.AppTheme_Dark
            else -> defaultTheme  // default theme
        }
        spm.saveThemeResId(themeResId = newTheme)
        return newTheme
    }
}
