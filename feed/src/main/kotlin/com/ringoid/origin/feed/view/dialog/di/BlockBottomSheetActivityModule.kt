package com.ringoid.origin.feed.view.dialog.di

import com.ringoid.origin.feed.view.dialog.BlockBottomSheetActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BlockBottomSheetActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeBlockBottomSheetActivityInjector(): BlockBottomSheetActivity
}
