package com.ringoid.origin.auth.view

import android.app.Application
import android.os.Build
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.manager.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.ClearCachedBlockedProfileIdsUseCase
import com.ringoid.domain.interactor.feed.ClearCachedLmmProfileIdsUseCase
import com.ringoid.domain.interactor.image.ClearCachedImageRequestsUseCase
import com.ringoid.domain.interactor.image.ClearCachedUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesUseCase
import com.ringoid.domain.interactor.user.ClearLocalUserDataUseCase
import com.ringoid.domain.interactor.user.CreateUserProfileUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.style.APP_THEME
import com.ringoid.origin.style.ThemeUtils
import com.ringoid.utility.isAdultAge
import com.ringoid.widget.WidgetState
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val clearLocalUserDataUseCase: ClearLocalUserDataUseCase,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val clearCachedBlockedProfileIdsUseCase: ClearCachedBlockedProfileIdsUseCase,
    private val clearCachedLmmProfileIdsUseCase: ClearCachedLmmProfileIdsUseCase,
    private val clearCachedUserImagesUseCase: ClearCachedUserImagesUseCase,
    private val clearCachedImageRequestsUseCase: ClearCachedImageRequestsUseCase,
    private val clearMessagesUseCase: ClearMessagesUseCase,
    app: Application) : BaseViewModel(app) {

    private val calendar: Calendar by lazy { getApplication<BaseRingoidApplication>().calendar }

    val loginButtonEnableState by lazy { MutableLiveData<Boolean>() }
    val yearOfBirthEntryState by lazy { MutableLiveData<WidgetState>() }

    var gender: Gender? = null
        private set (value) {
            field = value
            enableLoginButton()
        }
    private var yearOfBirth: Int = 0

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        analyticsManager.fire(Analytics.SCREEN_SIGN_UP)
    }

    // --------------------------------------------------------------------------------------------
    fun login() {
        val essence = AuthCreateProfileEssence(
            yearOfBirth = yearOfBirth,
            sex = gender?.string ?: Gender.MALE.string /* safe null-check */,
            device = String.format("%s, %s, %s", Build.MODEL, Build.MANUFACTURER, Build.PRODUCT),
            osVersion = String.format("%s, %d", Build.VERSION.RELEASE, Build.VERSION.SDK_INT),
            privateKey = spm.createPrivateKeyIfNotExists(), referralId = spm.getReferralCode(),
            settings = app.userSettingsManager.getUserSettings())

        createUserProfileUseCase.source(params = Params().put(essence))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.CLOSE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully signed up, current user: $it")
                analyticsManager.fire(Analytics.AUTH_USER_PROFILE_CREATED, "yearOfBirth" to "${essence.yearOfBirth}", "sex" to essence.sex)
                app.userScopeProvider.onLogin()
            }, Timber::e)
    }

    fun onGenderSelect(gender: Gender) {
        this.gender = gender
    }

    fun onYearOfBirthChange(text: String) {
        yearOfBirthEntryState.value =
                text.takeIf { it.isEmpty() }
                    ?.let { WidgetState.NORMAL }
                    ?: run {
                        text.toIntOrNull()
                            ?.takeIf { isAdultAge(it, calendar) }
                            ?.let { yearOfBirth = it; WidgetState.ACTIVE }
                            ?: WidgetState.ERROR
                    }

        enableLoginButton()
    }

    // ------------------------------------------
    fun onLogout() {
        clearLocalUserDataUseCase.source()
            .doOnSubscribe {
                ChatInMemoryCache.clear()
                DebugLogUtil.clear()
                actionObjectPool.finalizePool()  // clear state of pool, if any
                app.userScopeProvider.onLogout()  // prevent pool from receiving new state, if subscribed
            }
            .andThen(clearCachedAlreadySeenProfileIdsUseCase.source())
            .andThen(clearCachedBlockedProfileIdsUseCase.source())
            .andThen(clearCachedLmmProfileIdsUseCase.source())
            .andThen(clearCachedUserImagesUseCase.source())
            .andThen(clearCachedImageRequestsUseCase.source())
            .andThen(clearMessagesUseCase.source())
            .autoDisposable(this)
            .subscribe({ Timber.i("Local user data has been cleared on logout") }, Timber::e)
    }

    // ------------------------------------------
    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(spm)
        viewState.value = ViewState.DONE(APP_THEME(newTheme))
        viewState.value = ViewState.IDLE
    }

    // --------------------------------------------------------------------------------------------
    private fun enableLoginButton(){
        val isValidYearOfBirth = yearOfBirthEntryState.value == WidgetState.ACTIVE
        loginButtonEnableState.value = isValidYearOfBirth && gender != null
    }
}
