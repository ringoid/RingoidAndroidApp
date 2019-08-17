package com.ringoid.origin.usersettings.view.settings

import android.app.Application
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.DeleteUserProfileUseCase
import com.ringoid.origin.style.APP_THEME
import com.ringoid.origin.style.ThemeUtils
import com.ringoid.origin.usersettings.view.base.BaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    fun deleteAccount() {
        deleteUserProfileUseCase.source()
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.i("Successfully deleted user account")
                analyticsManager.fire(Analytics.AUTH_USER_CALL_DELETE_HIMSELF)
            }, Timber::e)
    }

    // ------------------------------------------
    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(spm)
        viewState.value = ViewState.DONE(APP_THEME(newTheme))
        viewState.value = ViewState.IDLE
    }
}