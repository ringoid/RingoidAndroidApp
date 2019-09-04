package com.ringoid.origin.view.dialog.di

import com.ringoid.origin.view.dialog.SupportDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SupportDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeSupportDialogInjector(): SupportDialog
}
