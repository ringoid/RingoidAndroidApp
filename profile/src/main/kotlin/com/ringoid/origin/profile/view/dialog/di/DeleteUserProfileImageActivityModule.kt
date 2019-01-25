package com.ringoid.origin.profile.view.dialog.di

import com.ringoid.origin.profile.view.dialog.DeleteUserProfileImageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeleteUserProfileImageActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeDeleteUserProfileImageActivityInjector(): DeleteUserProfileImageActivity
}
