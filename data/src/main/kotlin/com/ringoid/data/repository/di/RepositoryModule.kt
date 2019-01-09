package com.ringoid.data.repository.di

import com.ringoid.data.executor.di.UseCaseExecutorModule
import com.ringoid.data.local.database.di.DatabaseModule
import com.ringoid.data.local.shared_prefs.di.SharedPrefsManagerModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.repository.feed.FeedRepository
import com.ringoid.data.repository.image.ImageRepository
import com.ringoid.data.repository.messenger.MessengerRepository
import com.ringoid.data.repository.user.UserRepository
import com.ringoid.domain.repository.feed.IFeedRepository
import com.ringoid.domain.repository.image.IImageRepository
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.domain.repository.user.IUserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RingoidCloudModule::class, DatabaseModule::class,
                    SharedPrefsManagerModule::class, UseCaseExecutorModule::class])
class RepositoryModule {

    @Provides @Singleton
    fun provideFeeRepository(repository: FeedRepository): IFeedRepository = repository

    @Provides @Singleton
    fun provideImageRepository(repository: ImageRepository): IImageRepository = repository

    @Provides @Singleton
    fun provideMessengerRepository(repository: MessengerRepository): IMessengerRepository = repository

    @Provides @Singleton
    fun provideUserRepository(repository: UserRepository): IUserRepository = repository
}
