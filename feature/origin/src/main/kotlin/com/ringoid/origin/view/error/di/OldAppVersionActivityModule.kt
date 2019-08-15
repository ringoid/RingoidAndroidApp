package com.ringoid.origin.view.error.di

import com.ringoid.origin.view.error.OldAppVersionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OldAppVersionActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeOldAppVersionActivityInjector(): OldAppVersionActivity
}
