package com.ringoid.origin.messenger.di

import com.ringoid.origin.messenger.view.ChatActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeChatActivityInjector(): ChatActivity
}
