package com.ringoid.main.view

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.base.viewmodel.ViewModelParams
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.interactor.actions.CountActionObjectsCachedInPoolUseCase
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLcUseCase
import com.ringoid.domain.interactor.image.GetUserImagesUseCase
import com.ringoid.domain.interactor.push.UpdatePushTokenUseCase
import com.ringoid.domain.interactor.user.ApplyReferralCodeUseCase
import com.ringoid.domain.interactor.user.UpdateUserSettingsUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.FiltersInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.model.essence.user.ReferralCodeEssenceUnauthorized
import com.ringoid.domain.model.essence.user.UpdateUserSettingsEssenceUnauthorized
import com.ringoid.domain.model.feed.Filters
import com.ringoid.domain.model.feed.NoFilters
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.push.PushUtils
import com.ringoid.origin.view.main.BaseMainViewModel
import com.ringoid.report.exception.WrongRequestParamsClientApiException
import com.ringoid.report.log.Report
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.age
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    getLcUseCase: GetLcUseCase, getUserImagesUseCase: GetUserImagesUseCase,
    private val filtersSource: IFiltersSource,
    private val clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    private val countActionObjectsCachedInPoolUseCase: CountActionObjectsCachedInPoolUseCase,
    private val applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val updateUserSettingsUseCase: UpdateUserSettingsUseCase, app: Application)
    : BaseMainViewModel(app) {

    private val badgeLikes by lazy { MutableLiveData<Boolean>() }
    private val badgeMessages by lazy { MutableLiveData<Boolean>() }
    private val badgeWarningProfile by lazy { MutableLiveData<Boolean>() }
    private val newLikesCount by lazy { MutableLiveData<Int>() }
    private val newMatchesCount by lazy { MutableLiveData<Int>() }
    private val newMessagesCount by lazy { MutableLiveData<Int>() }
    private val alertNoPushNotificationsOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    @DebugOnly private val closeDebugViewOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    internal fun badgeLikes(): LiveData<Boolean> = badgeLikes
    internal fun badgeMessages(): LiveData<Boolean> = badgeMessages
    internal fun badgeWarningProfile(): LiveData<Boolean> = badgeWarningProfile
    internal fun newLikesCount(): LiveData<Int> = newLikesCount
    internal fun newMatchesCount(): LiveData<Int> = newMatchesCount
    internal fun newMessagesCount(): LiveData<Int> = newMessagesCount
    internal fun alertNoPushNotificationsOneShot(): LiveData<OneShot<Boolean>> = alertNoPushNotificationsOneShot
    @DebugOnly internal fun closeDebugViewOneShot(): LiveData<OneShot<Boolean>> = closeDebugViewOneShot

    init {
        /**
         * LC data contains some not seen likes.
         */
        getLcUseCase.repository.badgeLikesSource()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLikes.value = it }, Timber::e)

        /**
         * LC data contains some not seen matches.
         *
         * @note Since matches are merged together with messages in one single feed - Messages Feed,
         *       any flag that there are some not seen matches should affect the appearance of badge
         *       for Messages Feed, i.e. fall through to update [MainViewModel.badgeMessages].
         */
        getLcUseCase.repository.badgeMatchesSource()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMessages.value = it }, Timber::e)  // updates badge for Messages Feed by intention

        /**
         * LC data contains some messages that are unread by the current user
         */
        getLcUseCase.repository.badgeMessengerSource()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMessages.value = it }, Timber::e)

        getLcUseCase.repository.lmmLoadFinishSource()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ HandledPushDataInMemory.dropCountsOfHandledPush() }, Timber::e)

        getLcUseCase.repository.newLikesCountSource()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it - HandledPushDataInMemory.getCountOfHandledPushLikes() }
            .filter { it > 0 }
            .autoDisposable(this)
            .subscribe({ newLikesCount.value = it }, Timber::e)

        getLcUseCase.repository.newMatchesCountSource()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it - HandledPushDataInMemory.getCountOfHandledPushMatches() }
            .filter { it > 0 }
            .autoDisposable(this)
            .subscribe({ newMatchesCount.value = it }, Timber::e)

        getLcUseCase.repository.newMessagesCountSource()
            .observeOn(AndroidSchedulers.mainThread())
            .map { it - HandledPushDataInMemory.getCountOfHandledPushMessages() }
            .filter { it > 0 }
            .autoDisposable(this)
            .subscribe({ newMessagesCount.value = it }, Timber::e)

        getUserImagesUseCase.repository.totalUserImagesSource()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeWarningProfile.value = it == 0 }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?, viewModelParams: ViewModelParams?) {
        super.onCreate(savedInstanceState, viewModelParams)
        savedInstanceState
            ?.let {
                analyticsManager.restore(it)
                ChatInMemoryCache.restore(it)
            }
            ?: run {
                analyticsManager.restore(spm)
                ChatInMemoryCache.restore(spm)
            }

        analyticsManager.setUser(spm)
        FiltersInMemoryCache.restore(spm)
        Report.setUser(spm.currentUserId())

        // filters not set, use default ones
        if (filtersSource.getFilters() == NoFilters) {
            if (spm.hasUserYearOfBirth()) {
                val yearOfBirth = spm.currentUserYearOfBirth()
                val age = age(yearOfBirth, app.calendar)
                val filters = when (spm.currentUserGender()) {
                    Gender.FEMALE -> Filters.createWithAgeRange(minAge = age, maxAge = age + 10)
                    else -> Filters.createWithAgeRange(minAge = age - 10, maxAge = age)
                }
                filtersSource.setFilters(filters)
            }
        }
    }

    override fun onFreshStart() {
        super.onFreshStart()
        onEachAppStart()

        clearCachedAlreadySeenProfileIdsUseCase.source()
            .autoDisposable(this)
            .subscribe({}, Timber::e)
    }

    internal fun onAppReOpen() {
        onEachAppStart()
    }

    override fun onStart() {
        super.onStart()
        alertNoPushNotificationsOneShot.value = OneShot(PushUtils.areNotificationsEnabled(getApplication()))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        analyticsManager.persist(outState)
        ChatInMemoryCache.persist(outState)
    }

    override fun onStop() {
        super.onStop()
        analyticsManager.persist(spm)
        ChatInMemoryCache.persist(spm)
        FiltersInMemoryCache.persist(spm)
        actionObjectPool.trigger()  // trigger when App went on background
    }

    // --------------------------------------------------------------------------------------------
    @DebugOnly
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventCloseDebugView(event: BusEvent.CloseDebugView) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        closeDebugViewOneShot.value = OneShot(true)
    }

    @DebugOnly
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventDebugInfo(event: BusEvent.DebugInfo) {
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        Timber.d("Received bus event: $event")
        countActionObjectsCachedInPoolUseCase.source()
            .autoDisposable(this)
            .subscribe({ DebugLogUtil.i("Total count of aobjs in pool: $it") }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    private fun onEachAppStart() {
        applyReferralCodeIfAny()
        updateUserSettings()
    }

    override fun onPushOpen() {
        super.onPushOpen()
        analyticsManager.fire(Analytics.PUSH_OPEN)
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
            .autoDisposable(this)
            .subscribe({ DebugLogUtil.i("Successfully uploaded Firebase push token: $token") }, Timber::e)
    }

    private fun updateUserSettings() {
        val settings = app.userSettingsManager.getUserSettings()
        val params = Params().put(UpdateUserSettingsEssenceUnauthorized(settings))
        updateUserSettingsUseCase.source(params = params)
            .autoDisposable(this)
            .subscribe({ DebugLogUtil.i("Successfully updated user settings") }, Timber::e)
    }
}
