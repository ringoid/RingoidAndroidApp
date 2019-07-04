package com.ringoid.origin.utils

import android.net.Uri

object ReferralUtils {

    fun getReferralCode(uri: Uri?): String? =
        uri?.let {
            when (it.scheme) {
                "http", "https" -> {
                    when (it.host) {
                        "ringoid.app",
                        "ringoid.app.link",
                        "ringoid.com" -> {
                            it.pathSegments
                                .takeIf { it.isNotEmpty() }
                                ?.let { it.last() }
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }
}
