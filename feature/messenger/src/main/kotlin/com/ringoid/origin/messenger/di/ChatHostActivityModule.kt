package com.ringoid.origin.messenger.di

import com.ringoid.origin.messenger.view.ChatHostActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ChatHostActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeChatHostActivityInjector(): ChatHostActivity
}
