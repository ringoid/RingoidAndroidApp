package com.ringoid.origin.utils

import android.content.Intent
import com.ringoid.utility.paths

object ReferralUtils {

    fun getReferralCode(intent: Intent?): String?=
        intent?.takeIf { !it.dataString.isNullOrBlank() }
              ?.takeIf {
                  it.dataString.startsWith("https://ringoid.app.link/") ||
                  it.dataString.startsWith("https://ringoid.com/r/")
              }
              ?.data?.paths()
              ?.find { it != "r" }
}
