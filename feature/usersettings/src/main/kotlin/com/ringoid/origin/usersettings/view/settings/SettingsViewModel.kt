package com.ringoid.origin.usersettings.view.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.system.PostToSlackUseCase
import com.ringoid.domain.interactor.user.DeleteUserProfileUseCase
import com.ringoid.origin.style.AppTheme
import com.ringoid.origin.style.ThemeUtils
import com.ringoid.origin.view.base.settings.SimpleBaseSettingsViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase,
    postToSlackUseCase: PostToSlackUseCase, app: Application)
    : SimpleBaseSettingsViewModel(postToSlackUseCase, app) {

    private val changeThemeOneShot by lazy { MutableLiveData<OneShot<AppTheme>>() }
    private val logoutUserOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun changeThemeOneShot(): LiveData<OneShot<AppTheme>> = changeThemeOneShot
    internal fun logoutUserOneShot(): LiveData<OneShot<Boolean>> = logoutUserOneShot

    fun deleteAccount() {
        deleteUserProfileUseCase.source()
            .doOnSubscribe { viewState.value = ViewState.LOADING }  // delete account progress
            .doOnComplete { logoutUserOneShot.value = OneShot(true) }  // delete account success
            .doOnError { viewState.value = ViewState.ERROR(it) }  // delete account failed
            .autoDisposable(this)
            .subscribe({
                Timber.i("Successfully deleted user account")
                analyticsManager.fire(Analytics.AUTH_USER_CALL_DELETE_HIMSELF)
            }, DebugLogUtil::e)
    }

    // ------------------------------------------
    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(styleSpm)
        changeThemeOneShot.value = OneShot(AppTheme(newTheme))
    }
}
