package com.ringoid.origin.profile.dialog.context_menu.di

import com.ringoid.origin.profile.dialog.context_menu.UserProfileContextMenuDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileContextMenuDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileContextMenuDialogInjector(): UserProfileContextMenuDialog
}
