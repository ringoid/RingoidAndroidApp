package com.ringoid.main.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.GetUserImagesUseCase
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.interactor.user.ApplyReferralCodeUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.origin.view.main.BaseMainViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, getUserImagesUseCase: GetUserImagesUseCase,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    updatePushTokenUseCase: UpdatePushTokenUseCase,
    updateUserSettingsUseCase: UpdateUserSettingsUseCase, app: Application)
    : BaseMainViewModel(applyReferralCodeUseCase, updatePushTokenUseCase, updateUserSettingsUseCase, app) {

    val badgeLmm by lazy { MutableLiveData<Boolean>() }
    val badgeWarningProfile by lazy { MutableLiveData<Boolean>() }

    init {
        getLmmUseCase.repository.lmmChanged
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLmm.value = it }, Timber::e)

        getUserImagesUseCase.repository.totalUserImages
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeWarningProfile.value = it == 0 }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        clearCachedAlreadySeenProfileIdsUseCase.source()
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }
}
