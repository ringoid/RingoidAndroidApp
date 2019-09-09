package com.ringoid.origin.usersettings.view.base

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.view.base.theme.ThemedBaseViewModel
import com.ringoid.utility.daysAgo
import com.ringoid.utility.fromTs
import com.uber.autodispose.lifecycle.autoDisposable
import java.util.*

abstract class BaseSettingsViewModel(private val postToSlackUseCase: PostToSlackUseCase, app: Application)
    : ThemedBaseViewModel(app) {

    private val suggestImprovementsOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun suggestImprovementsOneShot(): LiveData<OneShot<Boolean>> = suggestImprovementsOneShot

    internal fun suggestImprovements(text: String, tag: String?) {
        if (text.isBlank()) {
            return
        }

        val id = spm.currentUserId()
        val age = app.calendar.get(Calendar.YEAR) - spm.currentUserYearOfBirth()
        val createdAt = spm.currentUserCreateTs().takeIf { it > 0L }?.let { fromTs(it) }
        val daysAgo = spm.currentUserCreateTs().takeIf { it > 0L }?.let { daysAgo(it) }
        val gender = spm.currentUserGender()
        val ageGenderStr = mutableListOf<String>().apply {
            if (spm.hasUserYearOfBirth()) {
                add("$age")
            }
            if (gender != Gender.UNKNOWN) {
                add("${gender.short()}")
            }
        }
        // profile properties
        val profile = spm.getUserProfileProperties()
        val city = profile.whereLive.takeIf { it.isNotBlank() }?.let { " @ `$it`" } ?: ""
        val reportText = "*${ageGenderStr.joinToString(" ").trim()}* from `$tag`$city\n\n> ${text.replace("\n", "\n>")}\n\nAndroid ${BuildConfig.VERSION_NAME}\n${Build.MANUFACTURER} ${Build.MODEL}\n\n`$id`${if (createdAt.isNullOrBlank()) "" else " createdAt $createdAt ($daysAgo)"}"

        val channelId = when (gender) {
            Gender.FEMALE -> "CL9ATCU3B"
            else -> "CJDASTGTC"
        }
        val params = Params().put("channelId", channelId)
                             .put("text", reportText)
        postToSlackUseCase.source(params = params)
            .doOnComplete { suggestImprovementsOneShot.value = OneShot(true) }
            .autoDisposable(this)
            .subscribe({ spm.dropBigEditText() }, DebugLogUtil::e)
    }
}
