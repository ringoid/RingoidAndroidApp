package com.ringoid.data.remote.di

import com.ringoid.data.remote.api.RingoidCloud
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RingoidCloudModule::class])
interface CloudComponent {

    fun cloud(): RingoidCloud
}
