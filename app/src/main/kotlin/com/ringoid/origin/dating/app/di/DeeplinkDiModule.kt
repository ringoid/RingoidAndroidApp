package com.ringoid.origin.dating.app.di

import com.ringoid.origin.dating.app.deeplink.ReferralInstallListener
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class DeeplinkDiModule {

    @ContributesAndroidInjector
    abstract fun contributeReferralInstallListenerInjector(): ReferralInstallListener
}
