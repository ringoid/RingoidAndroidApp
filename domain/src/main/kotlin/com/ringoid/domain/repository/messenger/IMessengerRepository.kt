package com.ringoid.domain.repository.messenger

import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Message
import io.reactivex.Single

interface IMessengerRepository {

    fun getMessages(peerId: String): Single<List<Message>>

    fun sendMessage(essence: MessageEssence): Single<Message>
}
