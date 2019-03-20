package com.ringoid.origin.utils

import android.content.Context
import com.ringoid.origin.R
import com.ringoid.origin.error.UnsupportedLocaleException
import com.ringoid.utility.manager.LocaleManager

object LocaleUtils {

    fun getLangById(context: Context, langId: String): String {
        val resId = when (langId) {
            LocaleManager.LANG_EN -> R.string.settings_lang_en
            LocaleManager.LANG_BE, LocaleManager.LANG_RU,
            LocaleManager.LANG_UA, LocaleManager.LANG_UK -> R.string.settings_lang_ru
            else -> throw UnsupportedLocaleException(langId)
        }
        return context.getString(resId)
    }
}
