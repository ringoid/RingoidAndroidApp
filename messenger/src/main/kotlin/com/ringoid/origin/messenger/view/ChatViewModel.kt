package com.ringoid.origin.messenger.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.messenger.GetChatNewMessagesUseCase
import com.ringoid.domain.interactor.messenger.GetMessagesForPeerUseCase
import com.ringoid.domain.interactor.messenger.PollChatNewMessagesUseCase
import com.ringoid.domain.interactor.messenger.SendMessageToPeerUseCase
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.essence.action.ActionObjectEssence
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.print
import com.ringoid.origin.model.OnlineStatus
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LmmNavTab
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Flowable
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val getChatNewMessagesUseCase: GetChatNewMessagesUseCase,
    private val getMessagesForPeerUseCase: GetMessagesForPeerUseCase,
    private val pollChatNewMessagesUseCase: PollChatNewMessagesUseCase,
    private val sendMessageToPeerUseCase: SendMessageToPeerUseCase,
    app: Application) : BaseViewModel(app) {

    private data class ChatData(val chatId: String)

    val messages by lazy { MutableLiveData<List<Message>>() }
    val newMessages by lazy { MutableLiveData<List<Message>>() }
    val sentMessage by lazy { MutableLiveData<Message>() }
    val onlineStatus by lazy { MutableLiveData<OnlineStatus>() }

    private var chatData: ChatData? = null
    private var currentMessageList: List<Message> = emptyList()

    // --------------------------------------------------------------------------------------------
    /**
     * Get chat messages from the local storage. Those messages had been stored locally
     * when Lmm data fetching had completed.
     */
    fun getMessages(profileId: String) {
        chatData = ChatData(chatId = profileId)
        // The most recent message is the first one in list, positions ascending and message age is also ascending
        getMessagesForPeerUseCase.source(params = Params().put("chatId", profileId))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ msgs ->
                val peerMessagesCount = msgs.count { it.peerId != DomainUtil.CURRENT_USER_ID }
                ChatInMemoryCache.setPeerMessagesCountIfChanged(profileId = profileId, count = peerMessagesCount)
                currentMessageList = msgs.toMutableList().apply { removeAll { it.isLocal() } }
                messages.value = msgs
                startPollingChat(profileId = profileId, delay = 100L)
            }, Timber::e)
    }

    @Suppress("CheckResult")
    fun sendMessage(peerId: String, imageId: String = DomainUtil.BAD_ID, text: String?,
                    sourceFeed: LmmNavTab = LmmNavTab.MESSAGES) {
        if (text.isNullOrBlank()) {
            return  // don't send empty text
        }

        val essence = ActionObjectEssence(actionType = "MESSAGE", sourceFeed = sourceFeed.feedName, targetImageId = imageId, targetUserId = peerId)
        val message = MessageEssence(peerId = peerId, text = text.trim(), aObjEssence = essence)

        sendMessageToPeerUseCase.source(params = Params().put(message))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.DONE(CHAT_MESSAGE_SENT) }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                sentMessage.value = it

                // analytics
                with (analyticsManager) {
                    fire(Analytics.ACTION_USER_MESSAGE, "sourceFeed" to sourceFeed.feedName)
                    fireOnce(Analytics.AHA_FIRST_MESSAGE_SENT, "sourceFeed" to sourceFeed.feedName)
                    when (sourceFeed) {
                        LmmNavTab.LIKES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_LIKES)
                        LmmNavTab.MATCHES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_MATCHES)
                        LmmNavTab.MESSAGES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_MESSAGES)
                    }
                }
            }, Timber::e)
    }

    // --------------------------------------------------------------------------------------------
    private fun startPollingChat(profileId: String, delay: Long) {
        Flowable.timer(delay, TimeUnit.MILLISECONDS)
            .flatMap {
                pollChatNewMessagesUseCase.source(params = prepareGetChatParams(profileId))
                    .takeUntil { isStopped }  // stop polling if Chat screen was hidden
            }
            .doOnNext { if (it.id != profileId) Timber.e("IDS DIFF: ${it.id} / $profileId") }
            .autoDisposable(this)
            .subscribe(::handleChatUpdate, Timber::e)  // on error - fail silently
    }

    // --------------------------------------------------------------------------------------------
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onEventPushNewMessage(event: BusEvent.PushNewMessage) {
        Timber.d("Received bus event: $event")
        SentryUtil.breadcrumb("Bus Event", "event" to "$event")
        if (event.peerId == chatData?.chatId) {
            getChatNewMessagesUseCase.source(prepareGetChatParams(profileId = event.peerId))
                .autoDisposable(this)
                .subscribe(::handleChatUpdate, Timber::e)  // on error - fail silently
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun prepareGetChatParams(profileId: String): Params =
        Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                .put("chatId", profileId)

    private fun handleChatUpdate(chat: Chat) {
        val peerMessagesCount = chat.messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
        if (peerMessagesCount > 0) {
            ChatInMemoryCache.setPeerMessagesCountIfChanged(profileId = chat.id, count = peerMessagesCount + ChatInMemoryCache.getPeerMessagesCount(chat.id))
        }

        val list = mutableListOf<Message>()
            .apply {
                addAll(chat.messages.reversed())
                addAll(currentMessageList)
            }
        Timber.d("ListData: ${list.print()}")
        currentMessageList = ArrayList(list)  // clone list to avoid further modifications
        messages.value = list.apply { addAll(0, chat.unconsumedSentLocalMessages.reversed()) }
        Timber.i("LiveData: ${messages.value?.print()}")
        onlineStatus.value = OnlineStatus.from(chat.lastOnlineStatus, label = chat.lastOnlineText)
    }
}
