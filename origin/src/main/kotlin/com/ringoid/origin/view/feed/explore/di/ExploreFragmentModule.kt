package com.ringoid.origin.view.feed.explore.di

import com.ringoid.origin.view.feed.explore.ExploreFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ExploreFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeExploreFragmentInjector(): ExploreFragment
}
