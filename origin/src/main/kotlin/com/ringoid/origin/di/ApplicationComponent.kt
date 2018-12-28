package com.ringoid.origin.di

import com.ringoid.origin.RingoidApplication
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ApplicationModule::class])
interface ApplicationComponent : AndroidInjector<RingoidApplication>
