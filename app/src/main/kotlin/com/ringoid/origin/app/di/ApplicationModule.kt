package com.ringoid.origin.app.di

import com.ringoid.data.action_storage.di.ActionObjectPoolModule
import com.ringoid.data.repository.di.RepositoryModule
import dagger.Module

@Module(includes = [ActionObjectPoolModule::class, RepositoryModule::class])
class ApplicationModule
