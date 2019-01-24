package com.ringoid.origin.app.di

import android.app.Application
import android.content.Context
import com.ringoid.main.di.MainActivityModule
import com.ringoid.origin.app.RingoidApplication
import com.ringoid.origin.auth.di.LoginActivityModule
import com.ringoid.origin.feed.view.dialog.di.BlockBottomSheetActivityModule
import com.ringoid.origin.feed.view.dialog.di.BlockBottomSheetDialogModule
import com.ringoid.origin.feed.view.dialog.di.ReportBottomSheetDialogModule
import com.ringoid.origin.feed.view.explore.di.ExploreFragmentModule
import com.ringoid.origin.feed.view.lmm.di.LmmFragmentModule
import com.ringoid.origin.feed.view.lmm.like.di.LikesFeedFragmentModule
import com.ringoid.origin.feed.view.lmm.match.di.MatchesFeedFragmentModule
import com.ringoid.origin.feed.view.lmm.messenger.di.MessengerFragmentModule
import com.ringoid.origin.imagepreview.di.ImagePreviewActivityModule
import com.ringoid.origin.imagepreview.di.ImagePreviewFragmentModule
import com.ringoid.origin.messenger.di.ChatFragmentModule
import com.ringoid.origin.profile.view.profile.di.UserProfileFragmentModule
import com.ringoid.origin.usersettings.view.debug.di.DebugActivityModule
import com.ringoid.origin.usersettings.view.debug.di.DebugFragmentModule
import com.ringoid.origin.usersettings.view.di.SettingsActivityModule
import com.ringoid.origin.usersettings.view.di.SettingsFragmentModule
import com.ringoid.origin.usersettings.view.info.di.AboutDialogModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoActivityModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoFragmentModule
import com.ringoid.origin.view.dialog.di.StatusDialogModule
import com.ringoid.origin.view.splash.di.SplashActivityModule
import com.ringoid.origin.view.web.di.WebPageActivityModule
import com.ringoid.origin.view.web.di.WebPageFragmentModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class,
                      /** Screen modules */
                      AboutDialogModule::class,
                      BlockBottomSheetActivityModule::class, BlockBottomSheetDialogModule::class,
                      ReportBottomSheetDialogModule::class, ChatFragmentModule::class,
                      DebugActivityModule::class, DebugFragmentModule::class, ExploreFragmentModule::class,
                      ImagePreviewActivityModule::class, ImagePreviewFragmentModule::class,
                      LikesFeedFragmentModule::class, LmmFragmentModule::class,
                      LoginActivityModule::class, MainActivityModule::class,
                      MatchesFeedFragmentModule::class, MessengerFragmentModule::class,
                      UserProfileFragmentModule::class,
                      SettingsActivityModule::class, SettingsFragmentModule::class,
                      SettingsAppInfoActivityModule::class, SettingsAppInfoFragmentModule::class,
                      SplashActivityModule::class, StatusDialogModule::class,
                      WebPageActivityModule::class, WebPageFragmentModule::class])
interface ApplicationComponent : AndroidInjector<RingoidApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<RingoidApplication>() {
        @BindsInstance abstract fun application(app: Application): Builder
        @BindsInstance abstract fun applicationContext(context: Context): Builder
    }
}
