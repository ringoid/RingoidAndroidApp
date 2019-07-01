package com.ringoid.origin.usersettings.view.base

import android.app.Application
import android.os.Build
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.utility.daysAgo
import com.ringoid.utility.fromTs
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import java.util.*

abstract class BaseSettingsViewModel(
    private val postToSlackUseCase: PostToSlackUseCase, app: Application) : BaseViewModel(app) {

    fun suggestImprovements(text: String, tag: String?) {
        if (text.isBlank()) {
            return
        }

        val id = spm.currentUserId()
        val age = app.calendar.get(Calendar.YEAR) - spm.currentUserYearOfBirth()
        val createdAt = spm.currentUserCreateTs().takeIf { it > 0L }?.let { fromTs(it) }
        val daysAgo = spm.currentUserCreateTs().takeIf { it > 0L }?.let { daysAgo(it) }
        val gender = spm.currentUserGender()
        val reportText = "*$age ${gender.short()}* from `$tag`\n\n> ${text.replace("\n", "\n>")}\n\nAndroid ${BuildConfig.VERSION_NAME}\n${Build.MANUFACTURER} ${Build.MODEL}\n\n`$id`${if (createdAt.isNullOrBlank()) "" else " createdAt $createdAt ($daysAgo)"}"

        val params = Params().put("channelId", "CJDASTGTC")
                             .put("text", reportText)
        postToSlackUseCase.source(params = params)
            .doOnComplete { viewState.value = ViewState.DONE(SUGGEST_IMPROVEMENTS) }
            .autoDisposable(this)
            .subscribe({ spm.dropBigEditText() }, Timber::e)
    }
}
