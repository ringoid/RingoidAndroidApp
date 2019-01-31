package com.ringoid.origin.view.di

import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.origin.view.common.ImagePreviewReceiver
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BaseAppModule {

    @Provides @Singleton
    fun provideCropIwaResultReceiver(): IImagePreviewReceiver =
        ImagePreviewReceiver(CropIwaResultReceiver())
}
