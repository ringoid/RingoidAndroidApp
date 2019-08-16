package com.ringoid.repository.di

import com.ringoid.data.action_storage.di.ActionObjectPoolModule
import com.ringoid.data.executor.di.UseCaseExecutorModule
import com.ringoid.data.local.database.di.DaoModule
import com.ringoid.data.local.shared_prefs.di.SharedPrefsManagerModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.data.remote.di.SystemCloudModule
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.repository.debug.IDebugFeedRepository
import com.ringoid.domain.repository.debug.IDebugRepository
import com.ringoid.domain.repository.feed.IFeedRepository
import com.ringoid.domain.repository.image.IUserImageRepository
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.domain.repository.push.IPushRepository
import com.ringoid.domain.repository.system.ISystemRepository
import com.ringoid.domain.repository.user.IUserRepository
import com.ringoid.repository.UserInMemoryCache
import com.ringoid.repository.debug.DebugFeedRepository
import com.ringoid.repository.debug.DebugRepository
import com.ringoid.repository.feed.FeedRepository
import com.ringoid.repository.image.UserImageRepository
import com.ringoid.repository.messenger.MessengerRepository
import com.ringoid.repository.push.PushRepository
import com.ringoid.repository.system.SystemRepository
import com.ringoid.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RingoidCloudModule::class, SystemCloudModule::class, ActionObjectPoolModule::class,
                    DaoModule::class, SharedPrefsManagerModule::class, UseCaseExecutorModule::class])
class RepositoryModule {

    @Provides @Singleton @DebugOnly
    fun provideDebugRepository(repository: DebugRepository): IDebugRepository = repository

    @Provides @Singleton @DebugOnly
    fun provideDebugFeedRepository(repository: DebugFeedRepository): IDebugFeedRepository = repository

    @Provides @Singleton
    fun provideFeedRepository(repository: FeedRepository): IFeedRepository = repository

    @Provides @Singleton
    fun provideImageRepository(repository: UserImageRepository): IUserImageRepository = repository

    @Provides @Singleton
    fun provideMessengerRepository(repository: MessengerRepository): IMessengerRepository = repository

    @Provides @Singleton
    fun providePushRepository(repository: PushRepository): IPushRepository = repository

    @Provides @Singleton
    fun provideSystemRepository(repository: SystemRepository): ISystemRepository = repository

    @Provides @Singleton
    fun provideUserRepository(repository: UserRepository): IUserRepository = repository

    @Provides @Singleton
    fun provideUserInMemoryCache(cache: UserInMemoryCache): IUserInMemoryCache = cache
}
