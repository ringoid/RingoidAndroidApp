package com.ringoid.origin.view.main.di

import com.ringoid.origin.view.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module//(subcomponents = [MainActivitySubcomponent::class])
abstract class MainActivityModule {

//    @Binds @IntoMap @ClassKey(MainActivity::class)
//    abstract fun bindMainActivityInjectorFactory(builder: MainActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>

    @ContributesAndroidInjector
    abstract fun contrib(): MainActivity
}
