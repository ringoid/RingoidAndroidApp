package com.ringoid.data.remote.di

import com.ringoid.domain.di.ImageLoader
import dagger.Component
import okhttp3.OkHttpClient

@ImageLoader
@Component(modules = [ImageCloudModule::class])
interface ImageCloudComponent {

    fun networkClientForImageLoader(): OkHttpClient
}
