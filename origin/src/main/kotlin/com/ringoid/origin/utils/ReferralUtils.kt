package com.ringoid.origin.utils

import android.content.Intent
import com.ringoid.utility.paths

object ReferralUtils {

    const val URL1 = "https://ringoid.app/"

    fun getReferralCode(intent: Intent?): String? =
        intent?.dataString
              ?.takeIf { it.isNotBlank() }
              ?.takeIf {
                  it.startsWith("https://ringoid.app.link/") ||
                  it.startsWith("https://ringoid.com/r/")
              }
              ?.let { intent }
              ?.data?.paths()
              ?.find { it != "r" }

    fun getReferralCode(link: String?): String? =
        link?.takeIf { it.isNotBlank() }
            ?.takeIf { it.startsWith(URL1) }
            ?.takeIf { it.length > URL1.length }
            ?.let { it.substring(URL1.length) }
}
