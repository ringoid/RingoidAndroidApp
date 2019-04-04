package com.ringoid.origin.dating.app.di

import android.app.Application
import android.content.Context
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.domain.debug.IDebugLogDaoHelper
import com.ringoid.main.di.MainActivityModule
import com.ringoid.origin.auth.di.LoginActivityModule
import com.ringoid.origin.dating.app.RingoidApplication
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
import com.ringoid.origin.messenger.di.ChatHostActivityModule
import com.ringoid.origin.profile.dialog.di.DeleteUserProfileImageActivityModule
import com.ringoid.origin.profile.dialog.di.DeleteUserProfileImageDialogModule
import com.ringoid.origin.profile.view.di.UserProfileFragmentModule
import com.ringoid.origin.usersettings.view.debug.di.DebugActivityModule
import com.ringoid.origin.usersettings.view.debug.di.DebugFragmentModule
import com.ringoid.origin.usersettings.view.info.di.AboutDialogModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoActivityModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoFragmentModule
import com.ringoid.origin.usersettings.view.language.di.SettingsLangActivityModule
import com.ringoid.origin.usersettings.view.language.di.SettingsLangFragmentModule
import com.ringoid.origin.usersettings.view.settings.di.SettingsActivityModule
import com.ringoid.origin.usersettings.view.settings.di.SettingsFragmentModule
import com.ringoid.origin.view.dialog.di.StatusDialogModule
import com.ringoid.origin.view.error.di.NoNetworkConnectionActivityModule
import com.ringoid.origin.view.error.di.OldAppVersionActivityModule
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
                      DeleteUserProfileImageActivityModule::class, DeleteUserProfileImageDialogModule::class,
                      ReportBottomSheetDialogModule::class, ChatHostActivityModule::class, ChatFragmentModule::class,
                      DebugActivityModule::class, DebugFragmentModule::class, ExploreFragmentModule::class,
                      ImagePreviewActivityModule::class, ImagePreviewFragmentModule::class,
                      LikesFeedFragmentModule::class, LmmFragmentModule::class,
                      LoginActivityModule::class, MainActivityModule::class,
                      MatchesFeedFragmentModule::class, MessengerFragmentModule::class, NoNetworkConnectionActivityModule::class,
                      OldAppVersionActivityModule::class, UserProfileFragmentModule::class,
                      SettingsActivityModule::class, SettingsFragmentModule::class,
                      SettingsAppInfoActivityModule::class, SettingsAppInfoFragmentModule::class,
                      SettingsLangActivityModule::class, SettingsLangFragmentModule::class,
                      SplashActivityModule::class, StatusDialogModule::class,
                      WebPageActivityModule::class, WebPageFragmentModule::class])
interface ApplicationComponent : AndroidInjector<RingoidApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<RingoidApplication>() {
        @BindsInstance abstract fun application(app: Application): Builder
        @BindsInstance abstract fun applicationContext(context: Context): Builder
        abstract fun cloudModule(cloudModule: CloudModule): Builder
        abstract fun ringoidCloudModule(ringoidCloudModule: RingoidCloudModule): Builder
    }

    fun debugLogDao(): IDebugLogDaoHelper  // exposed access to debug-log database
}
