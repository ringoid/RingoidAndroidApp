package com.ringoid.origin.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.ringoid.domain.BuildConfig
import timber.log.Timber

const val RC_IMAGE_PREVIEW = 10000

fun Intent.withFlags(flags: Int): Intent = addFlags(flags)
fun Intent.singleTop(): Intent = addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

// ------------------------------------------------------------------------------------------------
// content://com.android.providers.media.documents/document/image:4561
const val CONTENT_URI = "content_uri"

private fun navigate(path: String, uri: String = BuildConfig.APPNAV): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("$uri$path"))

private fun navigate(path: String, payload: Intent? = null, uri: String = BuildConfig.APPNAV): Intent =
    navigate(path = path, uri = uri).apply { payload?.let { putExtras(it).putExtra(CONTENT_URI, it.data) } }

// ----------------------------------------------------------------------------
fun navigate(activity: Activity, path: String, rc: Int = 0, payload: Intent? = null) {
    Timber.v("navigate: path=$path, rc=$rc, payload=$payload")
    val intent = navigate(path = path, payload = payload)
    rc.takeIf { it != 0 }
      ?.let { activity.startActivityForResult(intent, rc) }
      ?: run { activity.startActivity(intent) }
}

fun navigate(fragment: Fragment, path: String, rc: Int = 0, payload: Intent? = null) {
    val intent = navigate(path = path, payload = payload)
    rc.takeIf { it != 0 }
        ?.let { fragment.startActivityForResult(intent, rc) }
        ?: run { fragment.startActivity(intent) }
}

fun navigateAndClose(activity: Activity, path: String, rc: Int = 0, payload: Intent? = null) {
    navigate(activity = activity, path = path, rc = rc, payload = payload)
    activity.finish()
}

fun navigateAndClose(fragment: Fragment, path: String, rc: Int = 0, payload: Intent? = null) {
    navigate(fragment = fragment, path = path, rc = rc, payload = payload)
    fragment.activity?.finish()
}

// ----------------------------------------------------------------------------
private fun blockingScreenIntent(path: String): Intent =
    navigate(path = path)
        .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

private fun logoutIntent(): Intent = blockingScreenIntent(path = "/login")
    .putExtra(Extras.EXTRA_LOGOUT, true)

fun blockingErrorScreen(activity: Activity, path: String) {
    blockingScreenIntent(path).let { activity.startActivity(it) }
}

fun blockingErrorScreen(fragment: Fragment, path: String) {
    blockingScreenIntent(path).let { fragment.startActivity(it) }
}

fun logout(activity: Activity) {
    logoutIntent().let { activity.startActivity(it) }
}

fun logout(fragment: Fragment) {
    logoutIntent().let { fragment.startActivity(it) }
}

fun splash(activity: Activity, path: String) {
    navigate(path = path, uri = "splash://ringoid.com").let(activity::startActivity)
    activity.finish()
}
