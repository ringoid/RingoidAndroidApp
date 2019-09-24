package com.ringoid.origin.feed.view.dialog.di

import com.ringoid.origin.feed.view.dialog.FeedItemContextMenuDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FeedItemContextMenuDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedItemContextMenuDialog(): FeedItemContextMenuDialog
}
