package com.ringoid.utility

import android.webkit.URLUtil

object ValueUtils {

    fun isValidLocation(latitude: Double, longitude: Double): Boolean =
        latitude != 0.0 || longitude != 0.0

    fun atCharSocialId(socialId: String): String =
        if (URLUtil.isValidUrl(socialId)) {
            socialId  // don't prefix uri
        } else {
            socialId.trim().firstOrNull()
                ?.let { c -> c.takeIf { it == '@' }?.let { socialId } ?: "@$socialId" }
                ?: ""
        }
}
