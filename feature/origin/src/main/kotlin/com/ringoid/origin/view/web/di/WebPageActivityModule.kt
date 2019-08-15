package com.ringoid.origin.view.web.di

import com.ringoid.origin.view.web.WebPageActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(subcomponents = [WebPageFragmentSubcomponent::class])
abstract class WebPageActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeWebPageActivityInjector(): WebPageActivity
}
