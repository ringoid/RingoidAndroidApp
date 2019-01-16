package com.ringoid.origin.feed.view.lmm.match.di

import com.ringoid.origin.feed.view.lmm.match.MatchesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MatchesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMatchesFeedFragmentInjector(): MatchesFeedFragment
}
