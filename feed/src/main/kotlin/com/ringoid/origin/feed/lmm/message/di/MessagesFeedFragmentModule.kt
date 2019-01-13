package com.ringoid.origin.feed.lmm.message.di

import com.ringoid.origin.feed.lmm.message.MessagesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessagesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMessagesFeedFragmentInjector(): MessagesFeedFragment
}
