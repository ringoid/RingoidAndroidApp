package com.ringoid.data.remote.di

import com.ringoid.datainterface.remote.IRingoidCloudFacade
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [RingoidCloudModule::class, CloudFacadeModule::class])
interface CloudComponent {

    fun cloud(): IRingoidCloudFacade
}
