package com.ringoid.origin.feed.view.dialog.di

import com.ringoid.origin.feed.view.dialog.ReportBottomSheetDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ReportBottomSheetDialogModule {

    @ContributesAndroidInjector
    abstract fun contributeReportBottomSheetDialogInjector(): ReportBottomSheetDialog
}
