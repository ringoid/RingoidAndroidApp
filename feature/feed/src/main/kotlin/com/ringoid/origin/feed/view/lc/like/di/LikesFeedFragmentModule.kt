package com.ringoid.origin.feed.view.lc.like.di

import com.ringoid.origin.feed.view.lc.like.LikesFeedFiltersFragment
import com.ringoid.origin.feed.view.lc.like.LikesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LikesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLikesFeedFragmentInjector(): LikesFeedFragment

    @ContributesAndroidInjector
    abstract fun contributeLikesFeedFiltersFragmentInjector(): LikesFeedFiltersFragment
}
