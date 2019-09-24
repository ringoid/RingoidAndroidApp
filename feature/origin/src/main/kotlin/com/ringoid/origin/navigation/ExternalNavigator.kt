package com.ringoid.origin.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Patterns
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import com.ringoid.base.ContextUtil
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.ringoid.utility.toast
import timber.log.Timber
import java.util.*

object ExternalNavigator {

    const val RC_EMAIL_SEND = 9800
    const val RC_GALLERY_GET_IMAGE = 9900
    const val RC_SETTINGS_LOCATION = 9901

    /* Browser */
    // --------------------------------------------------------------------------------------------
    fun openBrowser(context: Activity?, initUrl: String?) {
        if (context == null || initUrl.isNullOrBlank()) {
            return
        }

        var url = initUrl
        Timber.v("Browse to $url")
        if (url.startsWith("www.")) url = url.removePrefix("www.")
        if (URLUtil.isValidUrl(url)) {
            var uri = Uri.parse(url)
            if (TextUtils.isEmpty(uri.scheme)) {
                url = "https://$url"
                uri = Uri.parse(url)
            }

            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val message = "No Activity was found to open Browser!"
                Timber.e(message); context.toast(message)
            }
        } else {
            Timber.e("Input url [%s] is invalid !", url)
        }
    }

    /* Email */
    // --------------------------------------------------------------------------------------------
    fun openEmailComposer(activity: Activity, email: String, subject: String = "", body: String = "") {
        openEmailComposerIntent(email = email, subject = subject, body = body)
            .takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let { activity.startActivityForResult(it, RC_EMAIL_SEND) }
    }

    fun openEmailComposer(fragment: Fragment, email: String, subject: String = "", body: String = "") {
        fragment.activity?.let { activity ->
            openEmailComposerIntent(email = email, subject = subject, body = body)
                .takeIf { it.resolveActivity(activity.packageManager) != null }
                ?.let { fragment.startActivityForResult(it, RC_EMAIL_SEND) }
        }
    }

    fun emailDataProtectionOfficer(fragment: Fragment, bodyContent: CharSequence) {
        val body = String.format(AppRes.EMAIL_OFFICER_MAIL_SUBJECT, bodyContent)
        val subject = String.format(AppRes.EMAIL_SUPPORT_MAIL_SUBJECT, ContextUtil.appInfo())
        openEmailComposer(fragment, email = "data.protection@ringoid.com", subject = subject, body = body)
    }

    fun emailSupportTeam(fragment: Fragment, body: String = "", vararg extras: Pair<String, String>) {
        val subject = String.format(AppRes.EMAIL_SUPPORT_MAIL_SUBJECT, ContextUtil.appInfo())
        val xbody = "${extras.joinToString()}\n\n$body".trim()
        openEmailComposer(fragment, email = "support@ringoid.com", subject = subject, body = xbody)
    }

    private fun openEmailComposerIntent(email: String, subject: String = "", body: String = ""): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

    /* Camera and Gallery */
    // --------------------------------------------------------------------------------------------
    fun openGalleryToGetImage(activity: Activity) {
        openGalleryToGetImageIntent()
            .takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let { activity.startActivityForResult(it, RC_GALLERY_GET_IMAGE) }
    }

    fun openGalleryToGetImageFragment(fragment: Fragment) {
        fragment.activity?.let { activity ->
            openGalleryToGetImageIntent()
                .takeIf { it.resolveActivity(activity.packageManager) != null }
                ?.let { fragment.startActivityForResult(it, RC_GALLERY_GET_IMAGE) }
        }
    }

    private fun openGalleryToGetImageIntent(): Intent =
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }

    /* Google Play */
    // --------------------------------------------------------------------------------------------
    /**
     * https://play.google.com/store/apps/details?id=PACKAGE_NAME
     *
     * @see https://stackoverflow.com/questions/10816757/rate-this-app-link-in-google-play-store-app-on-the-phone
     */
    fun openGooglePlay(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.packageName)))
        } catch (e: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)))
        }
    }

    /* Location */
    // --------------------------------------------------------------------------------------------
    fun openLocationSettings(context: Context) {
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .takeIf { it.resolveActivity(context.packageManager) != null }
            ?.let { context.startActivity(it) }
    }

    fun openLocationSettingsForResult(activity: Activity) {
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            .takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let { activity.startActivityForResult(it, RC_SETTINGS_LOCATION) }
    }

    fun openLocationSettingsForResult(fragment: Fragment) {
        fragment.activity?.let { activity ->
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .takeIf { it.resolveActivity(activity.packageManager) != null }
                ?.let { fragment.startActivityForResult(it, RC_SETTINGS_LOCATION) }
        }
    }

    /* Social */
    // --------------------------------------------------------------------------------------------
    /**
     * @see https://stackoverflow.com/questions/15497261/open-instagram-user-profile?lq=1
     * @see https://stackoverflow.com/questions/21505941/intent-to-open-instagram-user-profile-on-android/23511180#23511180
     */
    fun openSocialInstagram(activity: Activity, instagramUserId: String?) {
        openSocial(activity, social = "instagram", host = "http://instagram.com/_u", socialUserId = instagramUserId)
    }

    /**
     * @see https://stackoverflow.com/questions/53344812/open-user-page-in-tiktok-app-on-android-with-intent
     */
    fun openSocialTiktok(activity: Activity, tiktokUserId: String?) {
        openSocial(activity, social = "tiktok", host = "http://www.tiktok.com", host4raw = "http://vm.tiktok.com", socialUserId = tiktokUserId, prefix = "@")
    }

    /**
     * Handles the following patterns for [socialUserId]:
     *
     * - @username
     * - username
     * - https://instagram.com/username
     * - instagram.com/username
     */
    private fun openSocial(activity: Activity, social: String, host: String, host4raw: String = host, socialUserId: String?, prefix: String = "") {
        socialUserId
            .takeIf { !it.isNullOrBlank() }
            ?.toLowerCase(Locale.getDefault())
            ?.removePrefix("@")  // prefix could be outer
            ?.removeSurrounding(prefix = "$social.com/", suffix = "")  // refine uri if schema is omitted
            ?.removePrefix("@")  // prefix could be inner
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let {
                if (it.contains(social, ignoreCase = true) &&
                    URLUtil.isValidUrl(it) &&
                    Patterns.WEB_URL.matcher(it).matches()) {
                    Uri.parse(it)
                } else {
                    if (prefix == "@") {
                        Uri.parse("$host/$prefix$it")
                    } else {
                        Uri.parse("$host4raw/$it")
                    }
                }
            }
            ?.let { uri -> Intent(Intent.ACTION_VIEW, uri) }
            ?.takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let { intent -> activity.startActivity(intent) }
            ?: run { activity.toast(R.string.error_common) }
    }
}
