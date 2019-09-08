package com.ringoid.origin.profile.dialog.context_menu.di

import com.ringoid.origin.profile.dialog.context_menu.UserProfileContextMenuActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileContextMenuActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileContextMenuActivityInjector(): UserProfileContextMenuActivity
}
