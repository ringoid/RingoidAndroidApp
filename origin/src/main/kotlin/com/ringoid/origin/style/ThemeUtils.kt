package com.ringoid.origin.style

import androidx.annotation.StyleRes
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.origin.R

object ThemeUtils {

    fun isDarkTheme(spm: ISharedPrefsManager): Boolean = spm.getThemeResId() == R.style.AppTheme_Dark

    @StyleRes
    fun switchTheme(spm: ISharedPrefsManager): Int {
        val currentTheme = spm.getThemeResId()
        val newTheme = when (currentTheme) {
            R.style.AppTheme_Dark -> R.style.AppTheme_Light
            R.style.AppTheme_Light -> R.style.AppTheme_Dark
            else -> R.style.AppTheme  // default theme
        }
        spm.saveThemeResId(themeResId = newTheme)
        return newTheme
    }
}
