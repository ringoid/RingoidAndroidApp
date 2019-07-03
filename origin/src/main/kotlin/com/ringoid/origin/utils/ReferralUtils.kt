package com.ringoid.origin.utils

import android.content.Intent
import com.ringoid.utility.paths

object ReferralUtils {

    fun getReferralCode(intent: Intent?): String?=
        intent?.dataString
              ?.takeIf { it.isNotBlank() }
              ?.takeIf {
                  it.startsWith("https://ringoid.app.link/") ||
                  it.startsWith("https://ringoid.com/r/")
              }
              ?.let { intent }
              ?.data?.paths()
              ?.find { it != "r" }
}
