package com.ringoid.origin.feed.view.dialog.di

import com.ringoid.origin.feed.view.dialog.BlockBottomSheetDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BlockBottomSheetDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeBlockBottomSheetDialog(): BlockBottomSheetDialog
}
