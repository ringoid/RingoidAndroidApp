package com.ringoid.origin.view.dialog.di

import com.ringoid.origin.view.dialog.BigEditTextDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BigEditTextDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeBigEditTextDialogInjector(): BigEditTextDialog
}
