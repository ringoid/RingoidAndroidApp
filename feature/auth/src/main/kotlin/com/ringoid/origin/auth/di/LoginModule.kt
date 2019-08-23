package com.ringoid.origin.auth.di

import com.ringoid.domain.memory.ILoginInMemoryCache
import com.ringoid.origin.auth.memory.LoginInMemoryCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LoginModule {

    @Provides @Singleton
    fun provideLoginInMemoryCache(): ILoginInMemoryCache = LoginInMemoryCache()
}
