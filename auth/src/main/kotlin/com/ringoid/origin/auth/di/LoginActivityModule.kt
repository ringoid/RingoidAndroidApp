package com.ringoid.origin.auth.di

import android.app.Activity
import com.ringoid.origin.auth.view.LoginActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [LoginActivitySubcomponent::class])
abstract class LoginActivityModule {

    @Binds @IntoMap @ClassKey(LoginActivity::class)
    abstract fun bindLoginActivityInjectorFactory(builder: LoginActivitySubcomponent.Builder): AndroidInjector.Factory<out Activity>
}
