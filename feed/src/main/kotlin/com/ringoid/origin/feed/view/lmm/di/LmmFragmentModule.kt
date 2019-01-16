package com.ringoid.origin.feed.lmm.di

import com.ringoid.origin.feed.lmm.LmmFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LmmFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLmmFragmentInjector(): LmmFragment
}
