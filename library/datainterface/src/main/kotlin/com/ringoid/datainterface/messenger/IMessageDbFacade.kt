package com.ringoid.datainterface.messenger

import com.ringoid.domain.model.messenger.Message
import io.reactivex.Single

interface IMessageDbFacade {

    fun countPeerMessages(): Single<Int>

    fun countPeerMessages(peerId: String): Single<Int>

    fun countUnreadMessages(): Single<Int>

    fun insertMessages(messages: Collection<Message>)
}
