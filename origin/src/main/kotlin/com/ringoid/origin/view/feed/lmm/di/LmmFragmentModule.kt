package com.ringoid.origin.view.feed.lmm.di

import com.ringoid.origin.view.feed.lmm.LmmFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LmmFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLmmFragmentInjector(): LmmFragment
}
