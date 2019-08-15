package com.ringoid.data.action_storage.di

import com.ringoid.data.action_storage.PersistActionObjectPool
import com.ringoid.data.local.shared_prefs.di.SharedPrefsManagerModule
import com.ringoid.data.remote.di.RingoidCloudModule
import com.ringoid.domain.action_storage.IActionObjectPool
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [RingoidCloudModule::class, SharedPrefsManagerModule::class])
class ActionObjectPoolModule {

    @Provides @Singleton
    fun provideActionObjectPool(pool: PersistActionObjectPool): IActionObjectPool = pool
}
