package com.ringoid.origin.imagepreview.di

import com.ringoid.origin.imagepreview.view.ImagePreviewFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ImagePreviewFragmentSubcomponent : AndroidInjector<ImagePreviewFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ImagePreviewFragment>()
}
