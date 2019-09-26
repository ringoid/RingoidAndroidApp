package com.ringoid.utility.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import com.ringoid.utility.targetVersion
import timber.log.Timber
import java.util.*

/**
 * @see https://proandroiddev.com/change-language-programmatically-at-runtime-on-android-5e6bc15c758
 */
class LocaleManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val LANG_EN = "en"
        const val LANG_BE = "be"
        const val LANG_RU = "ru"
        const val LANG_UA = "ua"
        const val LANG_UK = "uk"

        private const val LANG_KEY = "lang_key"

//        fun getLocale(resources: Resources) =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources.configuration.locales[0]
//            else resources.configuration.locale

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
    fun getLang(): String = prefs.getString(LANG_KEY, null) ?: Locale.getDefault().language

    /**
     * Sets up default locale. Call this method in [Activity.onCreate] and [Application.onCreate].
     *
     * @param context - either [Activity] or [Application.getApplicationContext].
     */
    fun initLocale(context: Context) {
        val deviceLang = getRealLocale(context).language
        val inAppLang = getLang()
        val lang = inAppLang
        Timber.v("Init locale: $lang, device($deviceLang), in-app($inAppLang)")
        setNewLocale(context, lang)
    }

    fun setNewLocale(context: Context, lang: String) {
        Timber.v("Set new locale: $lang")
        persist(lang)
        updateResources(context, lang)
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private fun getRealLocale(context: Context): Locale =
        if (targetVersion(Build.VERSION_CODES.N)) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }

    @Suppress("ApplySharedPref")
    private fun persist(lang: String) {
        /**
         * Use commit() instead of apply(), because sometimes we kill the application process
         * immediately which will prevent apply() to finish
         */
        prefs.edit().putString(LANG_KEY, lang).commit()
    }

    private fun updateResources(context: Context, lang: String) {
        val locale = Locale(lang).apply { Locale.setDefault(this) }

        val config = Configuration(context.resources.configuration).apply {
            if (targetVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
                setLocale(locale)
            } else {
                this.locale = locale
            }
        }
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        Timber.v("Finish update locale: ${locale.language} default(${Locale.getDefault().language})")
    }
}
