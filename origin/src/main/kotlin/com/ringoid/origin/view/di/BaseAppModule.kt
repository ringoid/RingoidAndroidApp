package com.ringoid.origin.view.di

import android.content.Context
import com.ringoid.base.IImagePreviewReceiver
import com.ringoid.base.manager.di.AppManagerModule
import com.ringoid.origin.view.common.ImagePreviewReceiver
import com.ringoid.utility.manager.LocaleManager
import com.steelkiwi.cropiwa.image.CropIwaResultReceiver
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Singleton

@Module(includes = [AppManagerModule::class])
class BaseAppModule {

    @Provides @Singleton
    fun provideCropIwaResultReceiver(applicationContext: Context): IImagePreviewReceiver =
        ImagePreviewReceiver(applicationContext, CropIwaResultReceiver())

    @Provides @Singleton
    fun provideRandomGenerator(): Random = Random()
}
