package com.ringoid.data.local.database.facade.messenger

import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.datainterface.messenger.IMessageDbFacade
import com.ringoid.domain.model.messenger.Message
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDbFacadeImpl @Inject constructor(private val dao: MessageDao) : IMessageDbFacade {

    override fun countPeerMessages(): Single<Int> = dao.countPeerMessages()

    override fun countPeerMessages(peerId: String): Single<Int> = dao.countPeerMessages(peerId)

    override fun countUnreadMessages(): Single<Int> = dao.countUnreadMessages()

    override fun insertMessages(messages: Collection<Message>) {
        messages.map { MessageDbo.from(it) }.also { dao.insertMessages(it) }
    }
}
