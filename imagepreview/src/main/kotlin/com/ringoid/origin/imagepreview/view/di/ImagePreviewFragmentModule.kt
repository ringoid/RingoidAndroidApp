package com.ringoid.origin.imagepreview.view.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.imagepreview.view.ImagePreviewFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ImagePreviewFragmentSubcomponent::class])
abstract class ImagePreviewFragmentModule {

    @Binds @IntoMap @FragmentKey(ImagePreviewFragment::class)
    abstract fun bindImagePreviewFragmentInjectorFactory(builder: ImagePreviewFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
