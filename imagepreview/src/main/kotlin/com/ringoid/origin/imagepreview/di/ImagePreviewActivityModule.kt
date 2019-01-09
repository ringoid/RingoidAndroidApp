package com.ringoid.origin.imagepreview.di

import com.ringoid.origin.imagepreview.view.ImagePreviewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(subcomponents = [ImagePreviewActivitySubcomponent::class])
abstract class ImagePreviewActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeImagePreviewActivityInjector(): ImagePreviewActivity
}
