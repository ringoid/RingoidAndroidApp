package com.ringoid.origin.navigation

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import com.ringoid.origin.R

object ExternalNavigator {

    const val RC_GALLERY_GET_IMAGE = 9900

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
