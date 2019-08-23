package com.ringoid.origin.usersettings.view.info.di

import com.ringoid.origin.usersettings.view.info.AboutDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AboutDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeAboutDialogInjector(): AboutDialog
}
