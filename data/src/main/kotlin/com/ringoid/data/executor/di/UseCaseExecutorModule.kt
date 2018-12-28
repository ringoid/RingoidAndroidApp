package com.ringoid.data.executor.di

import com.ringoid.data.executor.UseCasePostExecutorImpl
import com.ringoid.data.executor.UseCaseThreadExecutorImpl
import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Named
import javax.inject.Singleton

@Module
class UseCaseExecutorModule {

    @Provides @Singleton @Named("PostExecutor")
    fun provideScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @Provides @Singleton
    fun provideUseCaseThreadExecutor(executor: UseCaseThreadExecutorImpl): UseCaseThreadExecutor = executor

    @Provides @Singleton
    fun provideUseCasePostExecutor(executor: UseCasePostExecutorImpl): UseCasePostExecutor = executor
}
