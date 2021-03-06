package com.ringoid.origin.dating.app.deeplink

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
import com.ringoid.origin.messenger.deeplink.ChatDeepLinkModule
import com.ringoid.origin.messenger.deeplink.ChatDeepLinkModuleLoader
import com.ringoid.origin.profile.deeplink.ProfileDeepLinkModule
import com.ringoid.origin.profile.deeplink.ProfileDeepLinkModuleLoader
import com.ringoid.origin.rateus.deeplink.RateUsDeepLinkModule
import com.ringoid.origin.rateus.deeplink.RateUsDeepLinkModuleLoader
import com.ringoid.origin.usersettings.deeplink.UserSettingsDeepLinkModule
import com.ringoid.origin.usersettings.deeplink.UserSettingsDeepLinkModuleLoader

@DeepLinkHandler(value = [AppDeepLinkModule::class, AuthDeepLinkModule::class, ChatDeepLinkModule::class,
                          FeedDeepLinkModule::class, ImagePreviewDeepLinkModule::class, MainDeepLinkModule::class,
                          OriginDeepLinkModule::class, ProfileDeepLinkModule::class, RateUsDeepLinkModule::class,
                          UserSettingsDeepLinkModule::class])
class DeepLinkHandlerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkDelegate = DeepLinkDelegate(
            AppDeepLinkModuleLoader(), AuthDeepLinkModuleLoader(), ChatDeepLinkModuleLoader(),
            FeedDeepLinkModuleLoader(), ImagePreviewDeepLinkModuleLoader(), MainDeepLinkModuleLoader(),
            OriginDeepLinkModuleLoader(), ProfileDeepLinkModuleLoader(), RateUsDeepLinkModuleLoader(),
            UserSettingsDeepLinkModuleLoader())
        /**
         * Delegate the deep link handling to DeepLinkDispatch.
         * It will start the correct Activity based on the incoming Intent URI
         */
        deepLinkDelegate.dispatchFrom(this)
        finish()  // finish this Activity since the correct one has been just started
    }
}
