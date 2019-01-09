package com.ringoid.origin.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.ringoid.domain.BuildConfig
import timber.log.Timber

// content://com.android.providers.media.documents/document/image:4561
const val CONTENT_URI = "content_uri"

private fun navigate(path: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("${BuildConfig.APPNAV}$path"))

fun navigate(activity: Activity, path: String, rc: Int = 0, payload: Intent? = null) {
    Timber.v("navigate: path=$path, rc=$rc, payload=$payload")
    val intent = navigate(path).apply {
        payload?.let { putExtras(it).putExtra(CONTENT_URI, it.data) }
    }
    rc.takeIf { it != 0 }
      ?.let { activity.startActivityForResult(intent, rc) }
      ?: run { activity.startActivity(intent) }
}

fun navigate(fragment: Fragment, path: String, rc: Int = 0) {
    val intent = navigate(path)
    rc.takeIf { it != 0 }
        ?.let { fragment.startActivityForResult(intent, rc) }
        ?: run { fragment.startActivity(intent) }
}

fun navigateAndClose(activity: Activity, path: String, rc: Int = 0, payload: Intent? = null) {
    navigate(activity = activity, path = path, rc = rc, payload = payload)
    activity.finish()
}

fun navigateAndClose(fragment: Fragment, path: String, rc: Int = 0) {
    navigate(fragment = fragment, path = path, rc = rc)
    fragment.activity?.finish()
}
