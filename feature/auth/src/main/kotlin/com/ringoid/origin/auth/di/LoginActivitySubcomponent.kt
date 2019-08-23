package com.ringoid.origin.auth.di

import com.ringoid.origin.auth.view.LoginActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface LoginActivitySubcomponent : AndroidInjector<LoginActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<LoginActivity>()
}
