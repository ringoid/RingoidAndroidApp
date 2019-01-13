package com.ringoid.origin.feed.lmm.like.di

import com.ringoid.origin.feed.lmm.like.LikesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LikesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLikesFeedFragmentInjector(): LikesFeedFragment
}
