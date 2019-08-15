package com.ringoid.origin.view.dialog.di

import com.ringoid.origin.view.dialog.StatusDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class StatusDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeStatusDialogInjector(): StatusDialog
}
