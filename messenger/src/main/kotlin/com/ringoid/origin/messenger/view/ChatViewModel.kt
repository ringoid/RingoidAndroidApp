package com.ringoid.origin.messenger.view

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.Message
import javax.inject.Inject

class ChatViewModel @Inject constructor(app: Application) : BaseViewModel(app) {

    val messages by lazy { MutableLiveData<List<Message>>() }

    fun getMessages() {
        // TODO
        messages.value = listOf(
            Message(peerId = "peer1", text = "Hello!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Glad to see you"),
            Message(peerId = "peer1", text = "Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "Go to eat food!"),
            Message(peerId = "peer1", text = "Hello!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Glad to see you"),
            Message(peerId = "peer1", text = "Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "Go to eat food!"),
            Message(peerId = "peer1", text = "Hello!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Glad to see you"),
            Message(peerId = "peer1", text = "Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "Go to eat food!"),
            Message(peerId = "peer1", text = "Hello!"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Howdy. Have a plans for tonight?"),
            Message(peerId = DomainUtil.CURRENT_USER_ID, text = "Glad to see you"),
            Message(peerId = "peer1", text = "Okay at 19:30 pm!"),
            Message(peerId = "peer1", text = "Go to eat food!"))
    }
}
