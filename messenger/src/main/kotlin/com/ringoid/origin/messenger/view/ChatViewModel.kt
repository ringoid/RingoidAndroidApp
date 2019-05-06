package com.ringoid.origin.messenger.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.manager.analytics.Analytics
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.messenger.GetMessagesForPeerUseCase
import com.ringoid.domain.interactor.messenger.SendMessageToPeerUseCase
import com.ringoid.domain.memory.ChatInMemoryCache
import com.ringoid.domain.model.essence.action.ActionObjectEssence
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Message
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ChatViewModel @Inject constructor(
    private val getMessagesForPeerUseCase: GetMessagesForPeerUseCase,
    private val sendMessageToPeerUseCase: SendMessageToPeerUseCase,
    app: Application) : BaseViewModel(app) {

    val messages by lazy { MutableLiveData<List<Message>>() }
    val sentMessage by lazy { MutableLiveData<Message>() }

    fun getMessages(profileId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES) {
        val params = Params().put("chatId", profileId)
                             .put("sourceFeed", sourceFeed)
        // The most recent message is the first one in list, positions ascending and message age is also ascending
        getMessagesForPeerUseCase.source(params = params)
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                val peerMessagesCount = it.count { it.peerId != DomainUtil.CURRENT_USER_ID }
                ChatInMemoryCache.setPeerMessagesCountIfChanged(profileId = profileId, count = peerMessagesCount)
                messages.value = it
            }, Timber::e)
    }

    @Suppress("CheckResult")
    fun sendMessage(peerId: String, imageId: String = DomainUtil.BAD_ID, text: String?,
                    sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES) {
        if (text.isNullOrBlank()) {
            return  // don't send empty text
        }

        val essence = ActionObjectEssence(actionType = "MESSAGE", sourceFeed = sourceFeed, targetImageId = imageId, targetUserId = peerId)
        val message = MessageEssence(peerId = peerId, text = text?.trim() ?: "", aObjEssence = essence)
        sendMessageToPeerUseCase.source(params = Params().put(message))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.DONE(CHAT_MESSAGE_SENT(it)) }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({
                sentMessage.value = it

                // analytics
                with (analyticsManager) {
                    fire(Analytics.ACTION_USER_MESSAGE, "sourceFeed" to sourceFeed)
                    fireOnce(Analytics.AHA_FIRST_MESSAGE_SENT, "sourceFeed" to sourceFeed)
                    when (sourceFeed) {
                        DomainUtil.SOURCE_FEED_LIKES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_LIKES)
                        DomainUtil.SOURCE_FEED_MATCHES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_MATCHES)
                        DomainUtil.SOURCE_FEED_MESSAGES -> fire(Analytics.ACTION_USER_MESSAGE_FROM_MESSAGES)
                    }
                }
            }, Timber::e)
    }
}
