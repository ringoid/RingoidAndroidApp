package com.ringoid.origin.app.di

import com.ringoid.data.repository.di.RepositoryModule
import com.ringoid.origin.auth.di.LoginModule
import com.ringoid.origin.view.di.BaseAppModule
import dagger.Module

@Module(includes = [BaseAppModule::class, LoginModule::class, RepositoryModule::class])
class ApplicationModule
