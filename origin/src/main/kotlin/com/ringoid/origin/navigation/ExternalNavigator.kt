package com.ringoid.origin.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.webkit.URLUtil
import androidx.fragment.app.Fragment
import com.ringoid.origin.R
import com.ringoid.utility.toast
import timber.log.Timber

object ExternalNavigator {

    const val RC_EMAIL_SEND = 9800
    const val RC_GALLERY_GET_IMAGE = 9900

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
                // TODO: localize
                val message = "No Activity was found to open Browser!"
                Timber.e(message); toast(context, message)
            }
        } else {
            Timber.e("Input url [%s] is invalid !", url)
        }
    }

    /* Email */
    // --------------------------------------------------------------------------------------------
    fun openEmailComposer(activity: Activity, email: String, subject: String) {
        openEmailComposerIntent(email = email, subject = subject)
            .takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let {
                val intent = Intent.createChooser(it, activity.resources.getString(R.string.common_compose_email))
                activity.startActivityForResult(intent, RC_EMAIL_SEND)
            }
    }

    fun openEmailComposer(fragment: Fragment, email: String, subject: String) {
        fragment.activity?.let { activity ->
            openEmailComposerIntent(email = email, subject = subject)
                .takeIf { it.resolveActivity(activity.packageManager) != null }
                ?.let {
                    val intent = Intent.createChooser(it, activity.resources.getString(R.string.common_compose_email))
                    fragment.startActivityForResult(intent, RC_EMAIL_SEND)
                }
        }
    }

    private fun openEmailComposerIntent(email: String, subject: String): Intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "")
        }

    /* Camera and Gallery */
    // --------------------------------------------------------------------------------------------
    fun openGalleryToGetImage(activity: Activity) {
        openGalleryToGetImageIntent()
            .takeIf { it.resolveActivity(activity.packageManager) != null }
            ?.let {
                val intent = Intent.createChooser(it, activity.resources.getString(R.string.common_choose_image))
                activity.startActivityForResult(intent, RC_GALLERY_GET_IMAGE)
            }
    }

    fun openGalleryToGetImageFragment(fragment: Fragment) {
        fragment.activity?.let { activity ->
            openGalleryToGetImageIntent()
                .takeIf { it.resolveActivity(activity.packageManager) != null }
                ?.let {
                    val intent = Intent.createChooser(it, activity.resources.getString(R.string.common_choose_image))
                    fragment.startActivityForResult(intent, RC_GALLERY_GET_IMAGE)
                }
        }
    }

    private fun openGalleryToGetImageIntent(): Intent =
        Intent().apply {
            action = Intent.ACTION_OPEN_DOCUMENT
            type = "image/*"
        }
}
