package com.ringoid.origin.feed.view.lmm.di

import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.feed.view.lmm.base.LmmFeedFiltersFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module @Deprecated("LMM -> LC")
abstract class LmmFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLmmFragmentInjector(): LmmFragment

    @ContributesAndroidInjector
    abstract fun contributeLmmFeedFiltersFragmentInjector(): LmmFeedFiltersFragment
}
