package com.ringoid.origin.view.feed.lmm.like.di

import com.ringoid.origin.view.feed.lmm.like.LikesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LikesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLikesFeedFragmentInjector(): LikesFeedFragment
}
