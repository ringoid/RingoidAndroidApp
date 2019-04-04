package com.ringoid.origin.dating.app.di

import com.ringoid.data.manager.di.ManagerModule
import com.ringoid.data.remote.di.RemoteModule
import com.ringoid.data.repository.di.RepositoryModule
import com.ringoid.origin.auth.di.LoginModule
import com.ringoid.origin.view.di.BaseAppModule
import dagger.Module

@Module(includes = [BaseAppModule::class, LoginModule::class, ManagerModule::class,
                    RemoteModule::class, RepositoryModule::class])
class ApplicationModule
