package com.ringoid.main.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.WrongRequestParamsClientApiException
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.GetUserImagesUseCase
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.interactor.user.ApplyReferralCodeUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.origin.view.main.BaseMainViewModel
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    getLmmUseCase: GetLmmUseCase, getUserImagesUseCase: GetUserImagesUseCase,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase, app: Application)
    : BaseMainViewModel(app) {

    val badgeLmm by lazy { MutableLiveData<Boolean>() }
    val badgeWarningProfile by lazy { MutableLiveData<Boolean>() }
    val newLikesCount by lazy { MutableLiveData<Int>() }
    val newMatchesCount by lazy { MutableLiveData<Int>() }
    val newMessagesCount by lazy { MutableLiveData<Int>() }

    init {
        getLmmUseCase.repository.lmmChanged
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLmm.value = it }, Timber::e)

        getLmmUseCase.repository.newLikesCount
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ newLikesCount.value = it }, Timber::e)

        getLmmUseCase.repository.newMatchesCount
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ newMatchesCount.value = it }, Timber::e)

        getLmmUseCase.repository.newMessagesCount
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ newMessagesCount.value = it }, Timber::e)

        getUserImagesUseCase.repository.totalUserImages
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeWarningProfile.value = it == 0 }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate() {
        super.onCreate()
        analyticsManager.setUser(spm)
        SentryUtil.setUser(spm)
    }

    override fun onFreshStart() {
        super.onFreshStart()
        ChatInMemoryCache.restore(spm)
        onEachAppStart()

        clearCachedAlreadySeenProfileIdsUseCase.source()
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    override fun onAppReOpen() {
        super.onAppReOpen()
        onEachAppStart()
    }

    override fun onStop() {
        super.onStop()
        ChatInMemoryCache.persist(spm)
        actionObjectPool.trigger()
    }

    // --------------------------------------------------------------------------------------------
    private fun onEachAppStart() {
        applyReferralCodeIfAny()
        updateUserSettings()
    }

    private fun applyReferralCodeIfAny() {
        spm.getReferralCode()
            ?.takeIf { !it.isNullOrBlank() }
            ?.let { referralCode ->
                applyReferralCodeUseCase.source(params = Params().put(ReferralCodeEssenceUnauthorized(referralCode)))
                    .doOnComplete { spm.setReferralCode(null) }  // drop accepted referral code
                    .autoDisposable(this)
                    .subscribe({ DebugLogUtil.i("Referral code [$referralCode] has been accepted") },
                        { Timber.e(it)
                            when (it) {
                                is WrongRequestParamsClientApiException -> {
                                    DebugLogUtil.w("Referral code [$referralCode] has been declined")
                                    spm.setReferralCode(null)  // drop declined referral code
                                }
                            }
                        })
            }
    }

    fun updatePushToken(token: String) {
        val params = Params().put(PushTokenEssenceUnauthorized(token))
        updatePushTokenUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }

    private fun updateUserSettings() {
        val settings = app.userSettingsManager.getUserSettings()
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .subscribe({ DebugLogUtil.i("Successfully updated user settings") }, Timber::e)
    }
}
