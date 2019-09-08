package com.ringoid.origin.profile.dialog.delete.di

import com.ringoid.origin.profile.dialog.delete.DeleteUserProfileImageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module  @Deprecated("Replaced with context menu")
abstract class DeleteUserProfileImageActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeDeleteUserProfileImageActivityInjector(): DeleteUserProfileImageActivity
}
