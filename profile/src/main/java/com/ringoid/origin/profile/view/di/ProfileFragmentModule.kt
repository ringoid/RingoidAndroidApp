package com.ringoid.origin.profile.view.di

import com.ringoid.origin.profile.view.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileFragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeProfileFragmentInjector(): ProfileFragment
}
