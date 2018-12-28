package com.ringoid.origin.app.di

import com.ringoid.data.repository.di.RepositoryModule
import dagger.Module

@Module(includes = [RepositoryModule::class])
class ApplicationModule
