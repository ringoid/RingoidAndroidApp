package com.ringoid.utility.manager

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import timber.log.Timber

class LocaleManager(private val context: Context) {

    companion object {
        const val LANG_EN = "en"
        const val LANG_BE = "be"
        const val LANG_RU = "ru"
        const val LANG_UA = "ua"
        const val LANG_UK = "uk"

        fun initLocale(context: Context): ContextWrapper =
            LocaleManagerInternal.initLocale(context)

        fun resetActivityTitle(activity: Activity) {
            try {
                val info = activity.packageManager.getActivityInfo(activity.componentName, PackageManager.GET_META_DATA)
                if (info.labelRes != 0) {
                    activity.setTitle(info.labelRes)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e(e)
            }
        }
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun getLang(): String = LocaleManagerInternal.getLang(context)

    fun setNewLocale(lang: String): ContextWrapper =
        LocaleManagerInternal.setNewLocale(context, lang)
}
