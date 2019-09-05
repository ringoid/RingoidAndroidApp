package com.ringoid.origin.auth.view

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.image.ClearCachedImageRequestsUseCase
import com.ringoid.domain.interactor.image.ClearCachedUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesUseCase
import com.ringoid.domain.interactor.user.ClearLocalUserDataUseCase
import com.ringoid.domain.interactor.user.CreateUserProfileUseCase
import com.ringoid.report.log.Report
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.FiltersInMemoryCache
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.essence.user.AuthCreateProfileEssence
import com.ringoid.origin.BaseRingoidApplication
import com.ringoid.origin.style.AppTheme
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
    private val clearCachedLmmUseCase: ClearCachedLmmUseCase,
    private val clearCachedLmmProfileIdsUseCase: ClearCachedLmmProfileIdsUseCase,
    private val clearCachedLmmTotalCountsUseCase: ClearCachedLmmTotalCountsUseCase,
    private val clearCachedUserImagesUseCase: ClearCachedUserImagesUseCase,
    private val clearCachedImageRequestsUseCase: ClearCachedImageRequestsUseCase,
    private val clearMessagesUseCase: ClearMessagesUseCase,
    app: Application) : BaseViewModel(app) {

    private val calendar: Calendar by lazy { getApplication<BaseRingoidApplication>().calendar }

    private val changeThemeOneShot by lazy { MutableLiveData<OneShot<AppTheme>>() }
    private val loginUserOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val loginButtonEnableState by lazy { MutableLiveData<Boolean>() }
    private val yearOfBirthEntryState by lazy { MutableLiveData<WidgetState>() }
    internal fun changeThemeOneShot(): LiveData<OneShot<AppTheme>> = changeThemeOneShot
    internal fun loginUserOneShot(): LiveData<OneShot<Boolean>> = loginUserOneShot
    internal fun loginButtonEnableState(): LiveData<Boolean> = loginButtonEnableState
    internal fun yearOfBirthEntryState(): LiveData<WidgetState> = yearOfBirthEntryState

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
            privateKey = spm.createPrivateKeyIfNotExists(),
            referralId = spm.getReferralCode(),
            settings = app.userSettingsManager.getUserSettings())

        createUserProfileUseCase.source(params = Params().put(essence))
            .doOnSubscribe { viewState.value = ViewState.LOADING }  // sign up new user progress
            .doOnSuccess { loginUserOneShot.value = OneShot(true) }  // sign up new user success
            .doOnError { viewState.value = ViewState.ERROR(it) }  // sign up new user failed
            .autoDisposable(this)
            .subscribe({
                Timber.d("Successfully signed up, current user: $it")
                analyticsManager.enterUserScope()  // prepare analytics manager data for the new logged in user
                analyticsManager.fire(Analytics.AUTH_USER_PROFILE_CREATED, "yearOfBirth" to "${essence.yearOfBirth}", "sex" to essence.sex, "referralId" to "${essence.referralId}")
                app.userScopeProvider.onLogin()
            }, DebugLogUtil::e)
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
                FiltersInMemoryCache.clear()
                Report.clear()
                spm.onLogout()  // clean-up on logout
                actionObjectPool.finalizePool()  // clear state of pool, if any
                analyticsManager.exitUserScope(spm)  // clear analytics manager data for the current user
                app.userScopeProvider.onLogout()  // prevent pool from receiving new state, if subscribed
            }
            .andThen(clearCachedAlreadySeenProfileIdsUseCase.source())
            .andThen(clearCachedBlockedProfileIdsUseCase.source())
            .andThen(clearCachedLmmUseCase.source())
            .andThen(clearCachedLmmProfileIdsUseCase.source())
            .andThen(clearCachedLmmTotalCountsUseCase.source())
            .andThen(clearCachedUserImagesUseCase.source())
            .andThen(clearCachedImageRequestsUseCase.source())
            .andThen(clearMessagesUseCase.source())
            .autoDisposable(this)
            .subscribe({ Timber.i("Local user data has been cleared on logout") }, DebugLogUtil::e)
    }

    // ------------------------------------------
    fun switchTheme() {
        val newTheme = ThemeUtils.switchTheme(spm)
        changeThemeOneShot.value = OneShot(AppTheme(newTheme))
    }

    // --------------------------------------------------------------------------------------------
    private fun enableLoginButton(){
        val isValidYearOfBirth = yearOfBirthEntryState.value == WidgetState.ACTIVE
        loginButtonEnableState.value = isValidYearOfBirth && gender != null
    }
}
