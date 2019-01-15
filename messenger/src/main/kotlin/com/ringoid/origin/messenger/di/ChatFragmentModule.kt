package com.ringoid.origin.messenger.di

import com.ringoid.origin.messenger.view.ChatFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeChatFragmentInjector(): ChatFragment
}
