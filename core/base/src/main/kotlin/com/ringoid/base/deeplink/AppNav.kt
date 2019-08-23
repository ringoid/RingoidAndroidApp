package com.ringoid.base.deeplink

import com.airbnb.deeplinkdispatch.DeepLinkSpec

// Prefix all in-app navigation deep links with "appnav://ringoid.com/", trailing slash is mandatory
@DeepLinkSpec(prefix = ["appnav://ringoid.com/"])
@Retention(AnnotationRetention.BINARY)
annotation class AppNav(vararg val value: String)
