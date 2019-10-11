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
import com.ringoid.domain.interactor.messenger.UpdateChatUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IFiltersSource
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.domain.model.feed.LmmSlice
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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagesFeedViewModel @Inject constructor(
    private val updateChatUseCase: UpdateChatUseCase,
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
        // show badge and 'tap-to-refresh' popup on Feed screen
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
            .doOnNext {
                HandledPushDataInMemory.incrementCountOfHandledPushMatches()
                pushNewMatch.value = 0L  // for particle animation
            }
            .throttleFirst(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ if (shouldVibrate) app.vibrate() }, DebugLogUtil::e)

        /**
         * Update chats for particular feed items the push notifications correspond to.
         * Update appearance of feed items corresponding to that chats.
         *
         * Also show particle animation for new unread chats (for existing chats - don't show particle
         * animation even if some chats have become unread). See [UpdateChatUseCase] docs for explanation.
         *
         * Show 'tap-to-refresh' popup, if some chats have become unread, but corresponding
         * feed items are not visible on screen.
         *
         * Show badge always, since push notification always means that there are unread chats
         * have appeared.
         */
        incomingPushMessages
            .subscribeOn(Schedulers.computation())
            .map { (it as BusEvent.PushNewMessage).peerId }
            // consume push event and skip any updates if target Chat is currently open
            .filter { !ChatInMemoryCache.isChatOpen(chatId = it) }
            // group push notifications by their peerId and handle each group independently on each other
            .groupBy { it /** peerId */ }
            .flatMap { source ->  // one source for all push messages with the same peerId
                source
                    // handle the last push notification for the given chatId within timespan
                    .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS)
                    // internally update each particular not-opened Chat, this push notification belongs to
                    .flatMapSingle { peerId ->
                        val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                            .put("chatId", peerId)
                            .put("isChatOpen", ChatInMemoryCache.isChatOpen(chatId = peerId))

                        // UseCase will deliver it's result to Main thread
                        updateChatUseCase.source(params = params)
                            .map { (_, isNewUnreadChat) ->
                                DebugLogUtil.v("Success update chat by push [new: $isNewUnreadChat]: $peerId")
                                markFeedItemAsNotSeen(feedItemId = peerId)
                                if (isNewUnreadChat && !isStopped) {
                                    pushNewMessage.value = 0L  // for particle animation
                                }
                                // update appearance of Feed item in Messages Feed, that corresponds to Chat being processed here
                                pushMessageUpdateProfileOneShot.value = OneShot(peerId)
                                peerId
                            }
                            .doOnSuccess {
                                // don't show 'tap-to-refresh' popup, if update has occurred for currently visible feed items
                                if (!isFeedItemOnViewport(profileId = peerId)) {
                                    DebugLogUtil.v("Show refresh popup on chat update, which is beyond viewport")
                                    refreshOnPush.value = true  // show 'tap-to-refresh' popup on Feed screen
                                }
                            }
                            // allow to recover (by refresh) manually if a particular chat has failed to update
                            .doOnError {
                                DebugLogUtil.w("Failed to update chat by push: $peerId")
                                refreshOnPush.value = true
                            }
                            .onErrorResumeNext { Single.just(peerId) }  // don't fail channel on error and continue
                    }
            }
            // next goes update for global visual states, which could be debounced to avoid spam change due to massive come of push notifications
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({
                // show badge on Messages LC tab
                pushMessagesBadgeOneShot.value = OneShot(true)
            }, DebugLogUtil::e)

        // vibrate on incoming messages
        incomingPushMessagesEffect
            .throttleFirst(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe({ if (shouldVibrate) app.vibrate() }, DebugLogUtil::e)
    }

    // --------------------------------------------------------------------------------------------
    override fun countNotSeen(feed: List<FeedItem>): List<String> =
        feed.takeIf { it.isNotEmpty() }
            ?.let { items ->
                items.map { it.id to it.countOfPeerMessages() }
                     .filter { it.second > 0 }
                     .filter { it.second > ChatInMemoryCache.getPeerMessagesCount(it.first) }
                     .map { it.first }
            } ?: emptyList()

    // ------------------------------------------
    override fun getSourceFeed(): LcNavTab = LcNavTab.MESSAGES

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
    override fun handleVisibleHintChange(isVisibleToUser: Boolean) {
        super.handleVisibleHintChange(isVisibleToUser)
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
        markFeedItemAsSeen(feedItemId = profileId)  // on chat close
    }

    /* Event Bus */
    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMatch(event: BusEvent.PushNewMatch) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        incomingPushMatch.onNext(event)  // for badge and 'tap-to-refresh' popup
        if (!isStopped) {
            incomingPushMatchEffect.onNext(0L)  // for particles and vibration
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        Report.breadcrumb("Bus Event ${event.javaClass.simpleName}", "event" to "$event")
        // consume push event and skip any updates if target Chat is currently open
        if (!ChatInMemoryCache.isChatOpen(chatId = event.peerId)) {
            // for update unopened chats, show badge, particles and 'tap-to-refresh' popup
            incomingPushMessages.onNext(event)
            if (!isStopped) {
                incomingPushMessagesEffect.onNext(0L)  // for vibration only
            }
        }
    }
}
