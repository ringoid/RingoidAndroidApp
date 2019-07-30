package com.ringoid.origin.feed.view.lmm.base

import android.app.Application
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.view.ViewState
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.feed.CacheBlockedProfileIdUseCase
import com.ringoid.domain.interactor.feed.ClearCachedAlreadySeenProfileIdsUseCase
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import com.ringoid.domain.interactor.feed.property.GetCachedFeedItemByIdUseCase
import com.ringoid.domain.interactor.feed.property.TransferFeedItemUseCase
import com.ringoid.domain.interactor.feed.property.UpdateFeedItemAsSeenUseCase
import com.ringoid.domain.interactor.image.CountUserImagesUseCase
import com.ringoid.domain.interactor.messenger.ClearMessagesForChatUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.memory.IUserInMemoryCache
import com.ringoid.domain.model.messenger.EmptyChat
import com.ringoid.origin.utils.ScreenHelper
import com.uber.autodispose.lifecycle.autoDisposable
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

@Deprecated("LMM -> LC")
abstract class BaseMatchesFeedViewModel(
    private val getChatUseCase: GetChatUseCase,
    getLmmUseCase: GetLmmUseCase,
    getCachedFeedItemByIdUseCase: GetCachedFeedItemByIdUseCase,
    updateFeedItemAsSeenUseCase: UpdateFeedItemAsSeenUseCase,
    transferFeedItemUseCase: TransferFeedItemUseCase,
    clearCachedAlreadySeenProfileIdsUseCase: ClearCachedAlreadySeenProfileIdsUseCase,
    clearMessagesForChatUseCase: ClearMessagesForChatUseCase,
    cacheBlockedProfileIdUseCase: CacheBlockedProfileIdUseCase,
    countUserImagesUseCase: CountUserImagesUseCase,
    userInMemoryCache: IUserInMemoryCache, app: Application)
    : BaseLmmFeedViewModel(
        getLmmUseCase,
        getCachedFeedItemByIdUseCase,
        updateFeedItemAsSeenUseCase,
        transferFeedItemUseCase,
        clearCachedAlreadySeenProfileIdsUseCase,
        clearMessagesForChatUseCase,
        cacheBlockedProfileIdUseCase,
        countUserImagesUseCase,
        userInMemoryCache, app) {

    private val incomingPushMessages = PublishSubject.create<BusEvent>()

    init {
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
            .doOnNext { peerId ->
                /**
                 * New messages have been received from push notification for profile with id [BusEvent.PushNewMessage.peerId],
                 * so need to update corresponding feed item, if any, to visually reflect change in unread messages count.
                 */
                ChatInMemoryCache.setPeerMessagesCount(profileId = peerId, count = 0)
                viewState.value = ViewState.DONE(PUSH_NEW_MESSAGES(profileId = peerId))
            }
            .debounce(DomainUtil.DEBOUNCE_PUSH, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable(this)
            .subscribe(::handlePushMessage, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    override fun onChatOpen(profileId: String, imageId: String) {
        super.onChatOpen(profileId, imageId)
        allowSingleUnchanged()
    }

    override fun onChatClose(profileId: String, imageId: String) {
        super.onChatClose(profileId, imageId)
        markFeedItemAsSeen(feedItemId = profileId)
    }

    override fun onRefresh() {
        super.onRefresh()
        allowSingleUnchanged()
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        incomingPushMessages.onNext(event)
    }

    protected open fun handlePushMessage(peerId: String) {
        // override in subclasses
    }

    // ------------------------------------------
    private var compareFlag = AtomicBoolean(true)

    private fun allowSingleUnchanged() {
        compareFlag.set(false)
    }

    private fun checkFlagAndDrop(): Boolean = compareFlag.getAndSet(true)
}
