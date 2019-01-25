package com.ringoid.origin.profile.view.dialog.di

import com.ringoid.origin.profile.view.dialog.DeleteUserProfileImageDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeleteUserProfileImageDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeDeleteUserProfileImageDialogInjector(): DeleteUserProfileImageDialog
}
