package com.ringoid.origin.messenger.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.messenger.GetChatNewMessagesUseCase
import com.ringoid.domain.interactor.messenger.GetChatUseCase
import com.ringoid.domain.interactor.messenger.GetMessagesForPeerUseCase
import com.ringoid.domain.interactor.messenger.SendMessageToPeerUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.essence.action.ActionObjectEssence
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.model.OnlineStatus
import com.ringoid.origin.utils.ScreenHelper
import com.ringoid.origin.view.main.LmmNavTab
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val getChatUseCase: GetChatUseCase,
    private val getChatNewMessagesUseCase: GetChatNewMessagesUseCase,
    private val getMessagesForPeerUseCase: GetMessagesForPeerUseCase,
    private val sendMessageToPeerUseCase: SendMessageToPeerUseCase,
    app: Application) : BaseViewModel(app) {

    val messages by lazy { MutableLiveData<List<Message>>() }
    val newMessages by lazy { MutableLiveData<List<Message>>() }
    val sentMessage by lazy { MutableLiveData<Message>() }
    val onlineStatus by lazy { MutableLiveData<OnlineStatus>() }

    private var currentMessageList: List<Message> = emptyList()

    /**
     * Get chat messages from the local storage. Those messages had been stored locally
     * when Lmm data fetching had completed.
     */
    fun getMessages(profileId: String, sourceFeed: LmmNavTab = LmmNavTab.MESSAGES) {
        val params = Params().put("chatId", profileId)
                             .put("sourceFeed", sourceFeed.feedName)
        // The most recent message is the first one in list, positions ascending and message age is also ascending
        getMessagesForPeerUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ msgs ->
                val peerMessagesCount = msgs.count { it.peerId != DomainUtil.CURRENT_USER_ID }
                ChatInMemoryCache.setPeerMessagesCountIfChanged(profileId = profileId, count = peerMessagesCount)
                currentMessageList = msgs
                messages.value = msgs
                startPollingChat(profileId = profileId, sourceFeed = sourceFeed, delay = 100L)
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
            .doOnSuccess { viewState.value = ViewState.DONE(CHAT_MESSAGE_SENT(it)) }
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
    private fun startPollingChat(profileId: String, sourceFeed: LmmNavTab, delay: Long) {
        val peerIdStr = if (BuildConfig.IS_STAGING) profileId.substring(0..3) else "<...>"
        val logStr = if (BuildConfig.IS_STAGING) "for p=$peerIdStr on ${sourceFeed.feedName}" else ""

        Flowable.timer(delay, TimeUnit.MILLISECONDS)
            .doOnSubscribe { Timber.v("Start polling chat $logStr".trim()) }
            .flatMap {
                val params = Params().put(ScreenHelper.getLargestPossibleImageResolution(context))
                                     .put("chatId", profileId)
                                     .put("sourceFeed", sourceFeed.feedName)
                getChatNewMessagesUseCase.source(params = params)
                    .doOnSubscribe { Timber.v("Poll chat $logStr".trim()) }
                    .repeatWhen { completed -> completed.delay(3000, TimeUnit.MILLISECONDS) }
            }
            .autoDisposable(this)
            .subscribe({ chat ->
                val peerMessagesCount = chat.messages.count { it.peerId != DomainUtil.CURRENT_USER_ID }
                if (peerMessagesCount > 0) {
                    ChatInMemoryCache.setPeerMessagesCountIfChanged(profileId = profileId, count = peerMessagesCount + ChatInMemoryCache.getPeerMessagesCount(profileId))
                    Timber.v("Count of messages from peer [$peerIdStr] has changed")
                } else Timber.v("Count of messages from peer [$peerIdStr] has NOT changed")

                val list = mutableListOf<Message>()
                    .apply {
                        addAll(chat.messages.reversed())
                        addAll(currentMessageList)
                    }
                currentMessageList = ArrayList(list)  // clone list to avoid further modifications
                messages.value = list.apply { addAll(0, chat.unconsumedSentLocalMessages.reversed()) }
                onlineStatus.value = OnlineStatus.from(chat.lastOnlineStatus, label = chat.lastOnlineText)
            }, Timber::e)  // on error - fail silently
    }
}
