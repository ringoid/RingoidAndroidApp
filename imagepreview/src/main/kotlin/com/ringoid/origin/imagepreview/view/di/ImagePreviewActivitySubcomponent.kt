package com.ringoid.origin.imagepreview.view.di

import com.ringoid.origin.imagepreview.view.ImagePreviewActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [ImagePreviewFragmentModule::class])
interface ImagePreviewActivitySubcomponent : AndroidInjector<ImagePreviewActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ImagePreviewActivity>()
}
