package com.ringoid.origin.style

import androidx.annotation.StyleRes
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.origin.R

object ThemeUtils {

    @StyleRes
    fun switchTheme(spm: ISharedPrefsManager, isChanged: Boolean): Int {
        val currentTheme = spm.getThemeResId(defaultThemeResId = R.style.AppTheme)
        if (!isChanged) {
            return currentTheme
        }

        val newTheme = when (currentTheme) {
            R.style.AppTheme_Dark -> R.style.AppTheme_Light
            R.style.AppTheme_Light -> R.style.AppTheme_Dark
            else -> R.style.AppTheme  // default theme
        }
        spm.saveThemeResId(themeResId = newTheme)
        return newTheme
    }
}
