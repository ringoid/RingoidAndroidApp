package com.ringoid.origin.app.di

import android.app.Application
import android.content.Context
import com.ringoid.origin.app.RingoidApplication
import com.ringoid.origin.auth.di.LoginActivityModule
import com.ringoid.origin.imagepreview.view.di.ImagePreviewActivityModule
import com.ringoid.origin.profile.view.profile.di.ProfileFragmentModule
import com.ringoid.main.di.MainActivityModule
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
                      DebugFragmentModule::class, ImagePreviewActivityModule::class,
                      LoginActivityModule::class, MainActivityModule::class,
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
