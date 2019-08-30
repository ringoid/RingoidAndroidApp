package com.ringoid.origin.feed.view.dialog.di

import com.ringoid.origin.feed.view.dialog.FeedItemContextMenuActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FeedItemContextMenuActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeFeedItemContextMenuActivityInjector(): FeedItemContextMenuActivity
}
