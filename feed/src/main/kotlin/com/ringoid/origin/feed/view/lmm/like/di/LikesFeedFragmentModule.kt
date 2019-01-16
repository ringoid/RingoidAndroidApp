package com.ringoid.origin.feed.view.lmm.like.di

import com.ringoid.origin.feed.view.lmm.like.LikesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LikesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLikesFeedFragmentInjector(): LikesFeedFragment
}
