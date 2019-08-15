package com.ringoid.origin.feed.view.lc.messenger.di

import com.ringoid.origin.feed.view.lc.messenger.MessagesFeedFiltersFragment
import com.ringoid.origin.feed.view.lc.messenger.MessagesFeedFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessagesFeedFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMessagesFeedFragmentInjector(): MessagesFeedFragment

    @ContributesAndroidInjector
    abstract fun contributeMessagesFeedFiltersFragmentInjector(): MessagesFeedFiltersFragment
}
