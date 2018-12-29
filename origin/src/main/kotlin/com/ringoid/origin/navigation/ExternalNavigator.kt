package com.ringoid.origin.navigation

import android.app.Activity
import android.content.Intent
import com.ringoid.origin.R

object ExternalNavigator {

    const val RC_GALLERY_GET_IMAGE = 10000

    // --------------------------------------------------------------------------------------------
    fun openGalleryToGetImage(activity: Activity) {
        Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }.takeIf { it.resolveActivity(activity.packageManager) != null }
         ?.let {
             val intent = Intent.createChooser(it, activity.resources.getString(R.string.common_choose_image))
             activity.startActivityForResult(intent, RC_GALLERY_GET_IMAGE)
         }
    }
}
