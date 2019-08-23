package com.ringoid.origin.profile.dialog.di

import com.ringoid.origin.profile.dialog.DeleteUserProfileImageDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeleteUserProfileImageDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeDeleteUserProfileImageDialogInjector(): DeleteUserProfileImageDialog
}
