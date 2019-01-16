package com.ringoid.data.action_storage.di

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.domain.action_storage.IActionObjectPool
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ActionObjectPoolModule {

    @Provides @Singleton
    fun provideActionObjectPool(pool: ActionObjectPool): IActionObjectPool = pool
}
