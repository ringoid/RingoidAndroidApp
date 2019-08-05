package com.ringoid.origin.view.filters.di

import com.ringoid.origin.view.filters.FiltersActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FiltersActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeFiltersActivityInjector(): FiltersActivity
}
