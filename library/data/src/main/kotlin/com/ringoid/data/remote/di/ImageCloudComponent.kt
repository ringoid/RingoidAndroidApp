package com.ringoid.data.remote.di

import com.ringoid.domain.di.ImageLoad
import dagger.Component
import okhttp3.OkHttpClient

@ImageLoad
@Component(modules = [ImageCloudModule::class])
interface ImageCloudComponent {

    fun networkClientForImageLoader(): OkHttpClient
}
