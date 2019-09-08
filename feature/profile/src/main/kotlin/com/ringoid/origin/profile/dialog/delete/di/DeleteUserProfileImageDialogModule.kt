package com.ringoid.origin.profile.dialog.delete.di

import com.ringoid.origin.profile.dialog.delete.DeleteUserProfileImageDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeleteUserProfileImageDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeDeleteUserProfileImageDialogInjector(): DeleteUserProfileImageDialog
}
