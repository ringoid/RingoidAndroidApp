package com.ringoid.origin.navigation

import android.content.Intent
import android.net.Uri
import com.ringoid.domain.BuildConfig

fun navigate(path: String): Intent =
    Intent(Intent.ACTION_VIEW, Uri.parse("${BuildConfig.APPNAV}$path"))
