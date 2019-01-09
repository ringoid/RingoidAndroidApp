package com.ringoid.origin.app.di

import android.app.Application
import android.content.Context
import com.ringoid.main.di.MainActivityModule
import com.ringoid.origin.app.RingoidApplication
import com.ringoid.origin.auth.di.LoginActivityModule
import com.ringoid.origin.imagepreview.view.di.ImagePreviewActivityModule
import com.ringoid.origin.imagepreview.view.di.ImagePreviewFragmentModule
import com.ringoid.origin.profile.view.profile.di.ProfileFragmentModule
import com.ringoid.origin.view.feed.explore.di.ExploreFragmentModule
import com.ringoid.origin.view.feed.lmm.di.LmmFragmentModule
import com.ringoid.origin.view.feed.lmm.like.di.LikesFeedFragmentModule
import com.ringoid.origin.view.feed.lmm.match.di.MatchesFeedFragmentModule
import com.ringoid.origin.view.feed.lmm.message.di.MessagesFeedFragmentModule
import com.ringoid.origin.view.messenger.di.ChatFragmentModule
import com.ringoid.origin.view.splash.di.SplashActivityModule
import com.ringoid.usersettings.view.debug.di.DebugFragmentModule
import com.ringoid.usersettings.view.di.SettingsFragmentModule
import com.ringoid.usersettings.view.info.di.SettingsAppInfoFragmentModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class,
                      /** Screen modules */
                      ChatFragmentModule::class, DebugFragmentModule::class,
                      ExploreFragmentModule::class,
                      ImagePreviewActivityModule::class, ImagePreviewFragmentModule::class,
                      LikesFeedFragmentModule::class, LmmFragmentModule::class,
                      LoginActivityModule::class, MainActivityModule::class,
                      MatchesFeedFragmentModule::class, MessagesFeedFragmentModule::class,
                      ProfileFragmentModule::class,
                      SettingsFragmentModule::class, SettingsAppInfoFragmentModule::class,
                      SplashActivityModule::class])
interface ApplicationComponent : AndroidInjector<RingoidApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<RingoidApplication>() {
        @BindsInstance abstract fun application(app: Application): Builder
        @BindsInstance abstract fun applicationContext(context: Context): Builder
    }
}
