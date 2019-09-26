package com.ringoid.utility.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import android.preference.PreferenceManager
import com.ringoid.utility.targetVersion
import timber.log.Timber
import java.util.*

internal object LocaleManagerInternal {

    private const val LANG_KEY = "lang_key"

    /**
     * Sets up default locale. Call this method in [Activity.onCreate] and [Application.onCreate].
     *
     * @param context - either [Activity] or [Application.getApplicationContext].
     */
    internal fun initLocale(context: Context): ContextWrapper {
        val deviceLang = getRealLocale(context).language
        val inAppLang = getLang(context)
        val lang = inAppLang
        Timber.v("Init locale: $lang, device($deviceLang), in-app($inAppLang)")
        return setNewLocale(context, lang)
    }

    internal fun setNewLocale(context: Context, lang: String): ContextWrapper {
        Timber.v("Set new locale: $lang")
        persist(context, lang)
        return updateResources(context, lang)
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    internal fun getLang(context: Context): String =
        getPrefs(context).getString(LANG_KEY, null) ?: Locale.getDefault().language

    private fun getPrefs(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private fun getRealLocale(context: Context): Locale =
        if (targetVersion(Build.VERSION_CODES.N)) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }

    @Suppress("ApplySharedPref")
    private fun persist(context: Context, lang: String) {
        /**
         * Use commit() instead of apply(), because sometimes we kill the application process
         * immediately which will prevent apply() to finish
         */
        getPrefs(context).edit().putString(LANG_KEY, lang).commit()
    }

    /**
     * Updates locale for the app.
     *
     * @see https://stackoverflow.com/questions/39705739/android-n-change-language-programmatically/40849142#40849142
     *      https://stackoverflow.com/questions/40221711/android-context-getresources-updateconfiguration-deprecated
     *      https://proandroiddev.com/change-language-programmatically-at-runtime-on-android-5e6bc15c758
     */
    private fun updateResources(context: Context, lang: String): ContextWrapper {
        val res = context.resources
        val config = res.configuration
        val locale = Locale(lang).apply { Locale.setDefault(this) }

        val xcontext = if (targetVersion(Build.VERSION_CODES.N)) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocale(locale)
            config.locales = localeList
            context.createConfigurationContext(config)
        } else if (targetVersion(Build.VERSION_CODES.JELLY_BEAN_MR1)) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
            context
        }

        Timber.v("Finish update locale: ${locale.language} default(${Locale.getDefault().language})")
        return ContextWrapper(xcontext)
    }
}
