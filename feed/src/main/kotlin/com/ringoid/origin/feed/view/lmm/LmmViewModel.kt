package com.ringoid.origin.feed.view.lmm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.DropLmmChangedStatusUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.feed.Lmm
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.utils.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LmmViewModel @Inject constructor(val getLmmUseCase: GetLmmUseCase,
    private val countUserImagesUseCase: CountUserImagesUseCase,
    private val dropLmmChangedStatusUseCase: DropLmmChangedStatusUseCase, app: Application)
    : BaseViewModel(app) {

    val badgeLikes by lazy { MutableLiveData<Boolean>() }
    val badgeMatches by lazy { MutableLiveData<Boolean>() }
    val badgeMessenger by lazy { MutableLiveData<Boolean>() }
    val clearAllFeeds by lazy { MutableLiveData<Int>() }
    val listScrolls by lazy { MutableLiveData<Int>() }
    var cachedLmm: Lmm? = null
        private set

    internal val pushNewLike by lazy { MutableLiveData<Long>() }
    internal val pushNewMatch by lazy { MutableLiveData<Long>() }
    internal val pushNewMessage by lazy { MutableLiveData<Long>() }

    init {
        getLmmUseCase.repository.badgeLikes
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeLikes.value = it }, Timber::e)

        getLmmUseCase.repository.badgeMatches
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMatches.value = it }, Timber::e)

        getLmmUseCase.repository.badgeMessenger
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ badgeMessenger.value = it }, Timber::e)
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onFreshStart() {
        super.onFreshStart()
        DebugLogUtil.i("Get LMM on fresh start")
        getLmm()
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventNoImagesOnProfile(event: BusEvent.NoImagesOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        dropLmmChangedStatusUseCase.source()  // drop changed status (red dot badges)
            .autoDisposable(this)
            .subscribe({ Timber.d("Badges on Lmm have been dropped because no images in user's profile") }, Timber::e)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewLike(event: BusEvent.PushNewLike) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushLikes()
        pushNewLike.value = 0L  // for particle animation
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewLike(event: BusEvent.PushNewMatch) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMatches()
        pushNewMatch.value = 0L  // for particle animation
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewLike(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMessages()
        pushNewMessage.value = 0L  // for particle animation
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnExplore(event: BusEvent.RefreshOnExplore) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Explore Feed screen leads Lmm screen to refresh as well
        DebugLogUtil.i("Get LMM on refresh Explore Feed")
        getLmm()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventRefreshOnProfile(event: BusEvent.RefreshOnProfile) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        // refresh on Profile screen leads Lmm screen to refresh as well
        DebugLogUtil.i("Get LMM on refresh Profile")
        getLmm()
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReOpenApp(event: BusEvent.ReOpenApp) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        DebugLogUtil.i("Get LMM on Application reopen")
        getLmm()  // app reopen leads Lmm screen to refresh as well
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventReStartWithTime(event: BusEvent.ReStartWithTime) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (event.msElapsed in 300000L..1557989300340L) {
            DebugLogUtil.i("App last open was more than 5 minutes ago, refresh Lmm...")
            getLmm()  // app reopen leads Lmm screen to refresh as well
        }
    }

    // ------------------------------------------
    private var getLmmLock = AtomicBoolean(false)

    private fun getLmm() {
        if (getLmmLock.get()) {
            DebugLogUtil.w("Lmm is already refreshing, skip this request to avoid duplicate Lmm calls")
            return
        }

        countUserImagesUseCase.source()
            .filter { it > 0 }  // user has images in profile
            .flatMapSingle {
                val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                                     .put("source", DomainUtil.SOURCE_FEED_PROFILE)
                getLmmUseCase.source(params = params)
            }
            .doOnSubscribe {
                getLmmLock.set(true)
                viewState.value = ViewState.CLEAR(ViewState.CLEAR.MODE_DEFAULT)
                viewState.value = ViewState.LOADING
            }
            .doOnSuccess { listScrolls.value = 0 }  // scroll to top position
            .doFinally {
                viewState.value = ViewState.IDLE
                getLmmLock.set(false)
            }
            .autoDisposable(this)
            .subscribe({ cachedLmm = it },
                        /**
                         * Typical case of error is when there is no images in profile, so 'Single.filter()'
                         * will emit zero items, though it must emit at least one by design. Instead,
                         * it will throw [NoSuchElementException] that will propagate here in 'onError()'.
                         * In this case, if any of Lmm feed screens are living, they should be purged.
                         */
                       { Timber.e(it); clearAllFeeds.value = ViewState.CLEAR.MODE_NEED_REFRESH })
    }
}
