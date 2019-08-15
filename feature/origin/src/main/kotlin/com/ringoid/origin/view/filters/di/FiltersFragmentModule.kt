package com.ringoid.origin.view.filters.di

import com.ringoid.origin.view.filters.FiltersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FiltersFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeFiltersFragmentInjector(): FiltersFragment
}
