package com.ringoid.origin.auth.di

import android.app.Activity
import com.ringoid.origin.auth.view.LoginActivity
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap

@Module(subcomponents = [LoginActivitySubcomponent::class])
abstract class LoginActivityModule {

    @Binds @IntoMap @ActivityKey(LoginActivity::class)
    abstract fun bindLoginActivityInjectorFactory(builder: LoginActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}
