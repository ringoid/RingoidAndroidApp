package com.ringoid.origin.dating.app.di

import android.app.Application
import android.content.Context
import android.os.HandlerThread
import com.ringoid.data.remote.di.CloudModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.di.SystemCloudModule
import com.ringoid.debug.IDebugLogDaoHelper
import com.ringoid.debug.barrier.IBarrierLogDaoHelper
import com.ringoid.main.di.MainActivityModule
import com.ringoid.origin.auth.di.LoginActivityModule
import com.ringoid.origin.dating.app.RingoidApplication
import com.ringoid.origin.feed.view.dialog.di.BlockBottomSheetDialogModule
import com.ringoid.origin.feed.view.dialog.di.FeedItemContextMenuActivityModule
import com.ringoid.origin.feed.view.dialog.di.FeedItemContextMenuDialogModule
import com.ringoid.origin.feed.view.dialog.di.ReportBottomSheetDialogModule
import com.ringoid.origin.feed.view.explore.di.ExploreFragmentModule
import com.ringoid.origin.feed.view.lc.like.di.LikesFeedFragmentModule
import com.ringoid.origin.feed.view.lc.messenger.di.MessagesFeedFragmentModule
import com.ringoid.origin.imagepreview.di.ImagePreviewActivityModule
import com.ringoid.origin.imagepreview.di.ImagePreviewFragmentModule
import com.ringoid.origin.messenger.di.ChatFragmentModule
import com.ringoid.origin.messenger.di.ChatHostActivityModule
import com.ringoid.origin.profile.dialog.context_menu.di.UserProfileContextMenuActivityModule
import com.ringoid.origin.profile.dialog.context_menu.di.UserProfileContextMenuDialogModule
import com.ringoid.origin.profile.view.di.UserProfileFragmentModule
import com.ringoid.origin.rateus.view.di.RateUsActivityModule
import com.ringoid.origin.rateus.view.di.RateUsDialogModule
import com.ringoid.origin.usersettings.view.debug.di.DebugActivityModule
import com.ringoid.origin.usersettings.view.debug.di.DebugFragmentModule
import com.ringoid.origin.usersettings.view.filters.di.SettingsFiltersActivityModule
import com.ringoid.origin.usersettings.view.filters.di.SettingsFiltersFragmentModule
import com.ringoid.origin.usersettings.view.info.di.AboutDialogModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoActivityModule
import com.ringoid.origin.usersettings.view.info.di.SettingsAppInfoFragmentModule
import com.ringoid.origin.usersettings.view.language.di.SettingsLangActivityModule
import com.ringoid.origin.usersettings.view.language.di.SettingsLangFragmentModule
import com.ringoid.origin.usersettings.view.profile.di.SettingsProfileActivityModule
import com.ringoid.origin.usersettings.view.profile.di.SettingsProfileFragmentModule
import com.ringoid.origin.usersettings.view.push.di.SettingsPushActivityModule
import com.ringoid.origin.usersettings.view.push.di.SettingsPushFragmentModule
import com.ringoid.origin.usersettings.view.settings.di.SettingsActivityModule
import com.ringoid.origin.usersettings.view.settings.di.SettingsFragmentModule
import com.ringoid.origin.view.dialog.di.BigEditTextDialogModule
import com.ringoid.origin.view.dialog.di.StatusDialogModule
import com.ringoid.origin.view.dialog.di.SupportDialogModule
import com.ringoid.origin.view.error.di.NoNetworkConnectionActivityModule
import com.ringoid.origin.view.error.di.OldAppVersionActivityModule
import com.ringoid.origin.view.filters.di.FiltersFragmentModule
import com.ringoid.origin.view.splash.di.SplashActivityModule
import com.ringoid.origin.view.web.di.WebPageActivityModule
import com.ringoid.origin.view.web.di.WebPageFragmentModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class, DeeplinkDiModule::class,
                      /** Screen modules */
                      AboutDialogModule::class, BigEditTextDialogModule::class,
                      FeedItemContextMenuActivityModule::class, FeedItemContextMenuDialogModule::class,
                      BlockBottomSheetDialogModule::class, ReportBottomSheetDialogModule::class,
                      ChatHostActivityModule::class, ChatFragmentModule::class,
                      DebugActivityModule::class, DebugFragmentModule::class, ExploreFragmentModule::class,
                      ImagePreviewActivityModule::class, ImagePreviewFragmentModule::class,
                      FiltersFragmentModule::class, LikesFeedFragmentModule::class,
                      LoginActivityModule::class, MainActivityModule::class,
                      MessagesFeedFragmentModule::class, NoNetworkConnectionActivityModule::class,
                      OldAppVersionActivityModule::class, UserProfileFragmentModule::class,
                      UserProfileContextMenuActivityModule::class, UserProfileContextMenuDialogModule::class,
                      RateUsActivityModule::class, RateUsDialogModule::class,
                      SettingsActivityModule::class, SettingsFragmentModule::class,
                      SettingsAppInfoActivityModule::class, SettingsAppInfoFragmentModule::class,
                      SettingsFiltersActivityModule::class, SettingsFiltersFragmentModule::class,
                      SettingsLangActivityModule::class, SettingsLangFragmentModule::class,
                      SettingsProfileActivityModule::class, SettingsProfileFragmentModule::class,
                      SettingsPushActivityModule::class, SettingsPushFragmentModule::class,
                      SplashActivityModule::class, StatusDialogModule::class, SupportDialogModule::class,
                      WebPageActivityModule::class, WebPageFragmentModule::class])
interface ApplicationComponent : AndroidInjector<RingoidApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<RingoidApplication>() {
        @BindsInstance abstract fun application(app: Application): Builder
        @BindsInstance abstract fun applicationContext(context: Context): Builder
        @BindsInstance abstract fun bgLooper(bgLooper: HandlerThread): Builder
        abstract fun cloudModule(cloudModule: CloudModule): Builder
        abstract fun ringoidCloudModule(ringoidCloudModule: RingoidCloudModule): Builder
        abstract fun systemCloudModule(systemCloudModule: SystemCloudModule): Builder
    }

    fun bgLooper(): HandlerThread
    fun barrierLogDao(): IBarrierLogDaoHelper  // exposed access to barrier-log database
    fun debugLogDao(): IDebugLogDaoHelper  // exposed access to debug-log database
}
