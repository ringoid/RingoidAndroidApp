package com.ringoid.origin.messenger.di

import com.ringoid.origin.messenger.view.MessengerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MessengerFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeChatFragmentInjector(): MessengerFragment
}
