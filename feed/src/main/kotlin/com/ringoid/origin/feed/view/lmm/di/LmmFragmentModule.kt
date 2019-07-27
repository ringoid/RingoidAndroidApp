package com.ringoid.origin.feed.view.lmm.di

import com.ringoid.origin.feed.view.lmm.LmmFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module @Deprecated("LMM -> LC")
abstract class LmmFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLmmFragmentInjector(): LmmFragment
}
