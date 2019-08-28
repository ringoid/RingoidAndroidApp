package com.ringoid.origin.usersettings.view.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.LiveEvent
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.DeleteUserProfileUseCase
import com.ringoid.origin.style.AppTheme
import com.ringoid.origin.style.ThemeUtils
import com.ringoid.origin.usersettings.view.base.BaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : BaseSettingsViewModel(postToSlackUseCase, app) {

    private val changeThemeOneShot by lazy { MutableLiveData<LiveEvent<AppTheme>>() }
    internal fun changeThemeOneShot(): LiveData<LiveEvent<AppTheme>> = changeThemeOneShot

    fun deleteAccount() {
        deleteUserProfileUseCase.source()
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.i("Successfully deleted user account")
                analyticsManager.fire(Analytics.AUTH_USER_CALL_DELETE_HIMSELF)
            }, DebugLogUtil::e)
    }

    // ------------------------------------------
    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(spm)
        changeThemeOneShot.value = LiveEvent(AppTheme(newTheme))
    }
}
