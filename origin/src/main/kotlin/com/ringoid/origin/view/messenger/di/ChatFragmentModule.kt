package com.ringoid.origin.view.messenger.di

import com.ringoid.origin.view.messenger.ChatFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeChatFragmentInjector(): ChatFragment
}
