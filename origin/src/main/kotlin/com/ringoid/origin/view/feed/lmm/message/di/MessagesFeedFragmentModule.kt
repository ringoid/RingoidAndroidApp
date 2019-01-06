package com.ringoid.origin.view.feed.lmm.message.di

import com.ringoid.origin.view.feed.lmm.message.MessagesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessagesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMessagesFeedFragmentInjector(): MessagesFeedFragment
}
