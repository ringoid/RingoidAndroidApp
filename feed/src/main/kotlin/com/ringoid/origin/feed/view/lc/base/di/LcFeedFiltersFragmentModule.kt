package com.ringoid.origin.feed.view.lc.base.di

import com.ringoid.origin.feed.view.lc.base.LcFeedFiltersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LcFeedFiltersFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLcFeedFiltersFragmentInjector(): LcFeedFiltersFragment
}
