package com.ringoid.origin.feed.view.lc.messenger

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ringoid.analytics.Analytics
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.viewmodel.OneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.*
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.interactor.messenger.GetChatOnlyUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.LmmSlice
import com.ringoid.domain.model.messenger.EmptyChat
import com.ringoid.origin.feed.misc.HandledPushDataInMemory
import com.ringoid.origin.feed.view.lc.base.BaseLcFeedViewModel
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LcNavTab
import com.ringoid.report.log.Report
import com.ringoid.utility.vibrate
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.functions.ObjectHelper
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MessagesFeedViewModel @Inject constructor(
    private val getChatUseCase: GetChatUseCase,
    private val getChatOnlyUseCase: GetChatOnlyUseCase,
    getLcUseCase: GetLcUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    filtersSource: IFiltersSource, userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLcFeedViewModel(
        getLcUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        filtersSource, userInMemoryCache, app) {

    private val incomingPushMatch = PublishSubject.create<BusEvent>()
    private val incomingPushMatchEffect = PublishSubject.create<Long>()
    private val incomingPushMessages = PublishSubject.create<BusEvent>()
    private val incomingPushMessagesEffect = PublishSubject.create<Long>()
    private val pushNewMatch by lazy { MutableLiveData<Long>() }
    private val pushNewMessage by lazy { MutableLiveData<Long>() }
    private val pushMatchesBadgeOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val pushMessagesBadgeOneShot by lazy { MutableLiveData<OneShot<Boolean>>() }
    private val pushMessageUpdateProfileOneShot by lazy { MutableLiveData<OneShot<String>>() }
    internal fun pushNewMatch(): LiveData<Long> = pushNewMatch
    internal fun pushNewMessage(): LiveData<Long> = pushNewMessage
    internal fun pushMatchesBadgeOneShot(): LiveData<OneShot<Boolean>> = pushMatchesBadgeOneShot
    internal fun pushMessagesBadgeOneShot(): LiveData<OneShot<Boolean>> = pushMessagesBadgeOneShot
    internal fun pushMessageUpdateProfileOneShot(): LiveData<OneShot<String>> = pushMessageUpdateProfileOneShot

    private var shouldVibrate: Boolean = true

    init {
        // show 'tap-to-refresh' popup on Feed screen
        incomingPushMatch
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Messages LC tab (as being for new Matches)
                pushMatchesBadgeOneShot.value = OneShot(true)
                // show 'tap-to-refresh' popup on Feed screen
                refreshOnPush.value = true
            }, DebugLogUtil::e)

        // show particle animation and vibrate
        incomingPushMatchEffect
            .doOnNext { pushNewMatch.value = 0L }  // for particle animation
            .throttleFirst(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ if (!isStopped && shouldVibrate) app.vibrate() }, DebugLogUtil::e)

        // show 'tap-to-refresh' popup on Feed screen and update chat for particular feed items
        incomingPushMessages
            .subscribeOn(Schedulers.computation())
            .map { (it as BusEvent.PushNewMessage).peerId }
            // consume push event and skip any updates if target Chat is currently open
            .filter { !ChatInMemoryCache.isChatOpen(chatId = it) }
            .distinctUntilChanged { prev, cur -> checkFlagAndDrop() && ObjectHelper.equals(prev, cur) }
            .flatMapSingle { peerId ->
                val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                                     .put("chatId", peerId)
                getChatUseCase.source(params = params)
                    .doOnSuccess { markFeedItemAsNotSeen(feedItemId = peerId) }
                    .onErrorResumeNext { Single.just(EmptyChat) }
                    .map { peerId }
            }  // use case will deliver it's result to Main thread
            .doOnNext { profileId -> pushMessageUpdateProfileOneShot.value = OneShot(profileId) }
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Messages LC tab
                pushMessagesBadgeOneShot.value = OneShot(true)
                // show 'tap-to-refresh' popup on Feed screen
                refreshOnPush.value = true
            }, DebugLogUtil::e)

        // show particle animation and vibrate
        incomingPushMessagesEffect
            .doOnNext { pushNewMessage.value = 0L }  // for particle animation
            .throttleFirst(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ if (!isStopped && shouldVibrate) app.vibrate() }, DebugLogUtil::e)
    }

    // ------------------------------------------
    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.takeIf { it.isNotEmpty() }
            ?.let { items ->
                items.map { it.id to it.countOfPeerMessages() }
                     .filter { it.second > 0 }
                     .filter { it.second > ChatInMemoryCache.getPeerMessagesCount(it.first) }
                     .map { it.first }
            } ?: emptyList()

    // ------------------------------------------
    private var compareFlag = AtomicBoolean(true)

    private fun allowSingleUnchanged() {
        compareFlag.set(false)
    }

    private fun checkFlagAndDrop(): Boolean = compareFlag.getAndSet(true)

    // ------------------------------------------
    override fun getSourceFeed(): LcNavTab = LcNavTab.MESSAGES
    override fun getFeedName(): String = DomainUtil.SOURCE_FEED_MESSAGES

    override fun sourceBadge(): Observable<Boolean> =
        getLcUseCase.repository.badgeMessengerSource()
            .doAfterNext {
                if (it && getUserVisibleHint()) {
                    analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
                }
            }

    override fun sourceFeed(): Observable<LmmSlice> = getLcUseCase.repository.feedMessagesSource()

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun handleUserVisibleHint(isVisibleToUser: Boolean) {
        super.handleUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {  /** switched to this LC tab */
            if (badgeIsOn) {  /** has new feed items */
                analyticsManager.fireOnce(Analytics.AHA_FIRST_MESSAGE_RECEIVED, "sourceFeed" to getFeedName())
            }
            shouldVibrate = spm.getUserPushSettings().pushVibration
        }
    }

    override fun onStart() {
        super.onStart()
        shouldVibrate = spm.getUserPushSettings().pushVibration
    }

    // --------------------------------------------------------------------------------------------
    override fun onChatClose(profileId: String, imageId: String) {
        super.onChatClose(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMatch(event: BusEvent.PushNewMatch) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMatches()
        incomingPushMatch.onNext(event)  // for 'tap-to-refresh' popup
        incomingPushMatchEffect.onNext(0L)
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        HandledPushDataInMemory.incrementCountOfHandledPushMessages()
        incomingPushMessages.onNext(event)
        if (!ChatInMemoryCache.isChatOpen(chatId = event.peerId)) {
            incomingPushMessagesEffect.onNext(0L)
        }
    }
}
