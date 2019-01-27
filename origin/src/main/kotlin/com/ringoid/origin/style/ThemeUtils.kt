package com.ringoid.origin.style

import androidx.annotation.StyleRes
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.origin.R

object ThemeUtils {

    fun isDarkLoginTheme(spm: ISharedPrefsManager): Boolean =
        spm.getMainThemeResId(defaultThemeResId = R.style.LoginTheme_Light) == R.style.LoginTheme_Dark

    fun isDarkMainTheme(spm: ISharedPrefsManager): Boolean =
        spm.getMainThemeResId(defaultThemeResId = R.style.AppTheme_Light) == R.style.AppTheme_Dark

    @StyleRes
    fun switchLoginTheme(spm: ISharedPrefsManager, isChanged: Boolean): Int {
        val currentTheme = spm.getLoginThemeResId(defaultThemeResId = R.style.LoginTheme_Light)
        if (!isChanged) {
            return currentTheme
        }

        val newTheme = when (currentTheme) {
            R.style.LoginTheme_Dark -> R.style.LoginTheme_Light
            R.style.LoginTheme_Light -> R.style.LoginTheme_Dark
            else -> R.style.LoginTheme_Light  // default theme
        }
        spm.saveLoginThemeResId(themeResId = newTheme)
        return newTheme
    }

    @StyleRes
    fun switchMainTheme(spm: ISharedPrefsManager, isChanged: Boolean): Int {
        val currentTheme = spm.getMainThemeResId(defaultThemeResId = R.style.AppTheme_Light)
        if (!isChanged) {
            return currentTheme
        }

        val newTheme = when (currentTheme) {
            R.style.AppTheme_Dark -> R.style.AppTheme_Light
            R.style.AppTheme_Light -> R.style.AppTheme_Dark
            else -> R.style.AppTheme  // default theme
        }
        spm.saveMainThemeResId(themeResId = newTheme)
        return newTheme
    }
}
