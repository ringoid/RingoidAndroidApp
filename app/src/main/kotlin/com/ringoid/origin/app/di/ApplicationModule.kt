package com.ringoid.origin.app.di

import com.ringoid.data.repository.di.RepositoryModule
import com.ringoid.origin.auth.di.LoginModule
import dagger.Module

@Module(includes = [LoginModule::class, RepositoryModule::class])
class ApplicationModule
