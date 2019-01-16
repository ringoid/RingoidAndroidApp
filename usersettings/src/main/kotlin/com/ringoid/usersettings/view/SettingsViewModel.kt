package com.ringoid.usersettings.view

import android.app.Application
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.user.DeleteUserProfileUseCase
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase,
    app: Application) : BaseViewModel(app) {

    fun deleteAccount() {
        deleteUserProfileUseCase.source()
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnComplete { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                // TODO: close and logout
            }, Timber::e)
    }
}
