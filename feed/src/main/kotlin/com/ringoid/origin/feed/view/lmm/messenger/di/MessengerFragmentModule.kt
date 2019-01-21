package com.ringoid.origin.feed.view.lmm.messenger.di

import com.ringoid.origin.feed.view.lmm.messenger.MessengerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessengerFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeChatFragmentInjector(): MessengerFragment
}
