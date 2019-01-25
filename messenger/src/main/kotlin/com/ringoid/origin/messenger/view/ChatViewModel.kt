package com.ringoid.origin.messenger.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.view.ViewState
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.interactor.base.Params
import com.ringoid.domain.interactor.messenger.SendMessageToPeerUseCase
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Message
import com.uber.autodispose.lifecycle.autoDisposable
import timber.log.Timber
import javax.inject.Inject

class ChatViewModel @Inject constructor(private val sendMessageToPeerUseCase: SendMessageToPeerUseCase, app: Application)
    : BaseViewModel(app) {

    val messages by lazy { MutableLiveData<List<Message>>() }
    val sentMessage by lazy { MutableLiveData<Message>() }

    fun getMessages() {
        // TODO: get messages properly
        // TODO: the most recent message is the first one in list, positions ascending and message age is also ascending
        messages.value = listOf(
            Message(peerId = "peer1", text = "1!"),  // NEW
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "1 Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "1 Glad to see you"),
            Message(peerId = "peer1", text = "1 Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "1 Go to eat food!"),
            Message(peerId = "peer1", text = "2!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "2 Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "2 Glad to see you"),
            Message(peerId = "peer1", text = "2 Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "2 Go to eat food!"),
            Message(peerId = "peer1", text = "3!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "3 Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "3 Glad to see you"),
            Message(peerId = "peer1", text = "3 Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "3 Go to eat food!"),
            Message(peerId = "peer1", text = "4!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "4 Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "4 Glad to see you"),
            Message(peerId = "peer1", text = "4 Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "4 Go to eat food!"))  // OLD
    }

    @Suppress("CheckResult")
    fun sendMessage(peerId: String, text: String) {
        sendMessageToPeerUseCase.source(params = Params().put(MessageEssence(peerId = peerId, text = text)))
            .doOnSubscribe { viewState.value = ViewState.LOADING }
            .doOnSuccess { viewState.value = ViewState.IDLE }
            .doOnError { viewState.value = ViewState.ERROR(it) }
            .autoDisposable(this)
            .subscribe({ sentMessage.value = it }, Timber::e)
    }
}
