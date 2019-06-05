package com.ringoid.origin.feed.view.lmm.messenger.di

import com.ringoid.origin.feed.view.lmm.messenger.MessagesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessagesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMessagesFeedFragmentInjector(): MessagesFeedFragment
}
