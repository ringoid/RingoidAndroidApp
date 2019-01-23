package com.ringoid.origin.app.deeplink

import android.app.Activity
import android.os.Bundle
import com.airbnb.deeplinkdispatch.DeepLinkHandler
import com.ringoid.main.deeplink.MainDeepLinkModule
import com.ringoid.main.deeplink.MainDeepLinkModuleLoader
import com.ringoid.origin.auth.deeplink.AuthDeepLinkModule
import com.ringoid.origin.auth.deeplink.AuthDeepLinkModuleLoader
import com.ringoid.origin.deeplink.OriginDeepLinkModule
import com.ringoid.origin.deeplink.OriginDeepLinkModuleLoader
import com.ringoid.origin.feed.deeplink.FeedDeepLinkModule
import com.ringoid.origin.feed.deeplink.FeedDeepLinkModuleLoader
import com.ringoid.origin.imagepreview.deeplink.ImagePreviewDeepLinkModule
import com.ringoid.origin.imagepreview.deeplink.ImagePreviewDeepLinkModuleLoader
import com.ringoid.origin.usersettings.deeplink.UserSettingsDeepLinkModule
import com.ringoid.origin.usersettings.deeplink.UserSettingsDeepLinkModuleLoader

@DeepLinkHandler(value = [AppDeepLinkModule::class, AuthDeepLinkModule::class, FeedDeepLinkModule::class,
                          ImagePreviewDeepLinkModule::class, MainDeepLinkModule::class,
                          OriginDeepLinkModule::class, UserSettingsDeepLinkModule::class])
class DeepLinkHandlerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkDelegate = DeepLinkDelegate(
            AppDeepLinkModuleLoader(), AuthDeepLinkModuleLoader(), FeedDeepLinkModuleLoader(),
            ImagePreviewDeepLinkModuleLoader(), MainDeepLinkModuleLoader(),
            OriginDeepLinkModuleLoader(), UserSettingsDeepLinkModuleLoader()
        )
        /**
         * Delegate the deep link handling to DeepLinkDispatch.
         * It will start the correct Activity based on the incoming Intent URI
         */
        deepLinkDelegate.dispatchFrom(this)
        finish()  // finish this Activity since the correct one has been just started
    }
}
