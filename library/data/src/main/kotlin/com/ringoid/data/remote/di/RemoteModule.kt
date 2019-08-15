package com.ringoid.data.remote.di

import com.ringoid.data.remote.ConnectionManager
import com.ringoid.domain.manager.IConnectionManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RemoteModule {

    @Provides @Singleton
    fun provideConnectionManager(manager: ConnectionManager): IConnectionManager = manager
}
