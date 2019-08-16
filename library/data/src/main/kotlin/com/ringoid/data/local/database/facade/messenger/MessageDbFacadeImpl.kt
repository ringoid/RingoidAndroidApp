package com.ringoid.data.local.database.facade.messenger

import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.datainterface.local.messenger.IMessageDbFacade
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDbFacadeImpl @Inject constructor(private val dao: MessageDao) : IMessageDbFacade {

    override fun addMessage(message: Message) {
        MessageDbo.from(message, unread = 0).also { dao.addMessage(it) }
    }

    override fun addMessages(messages: Collection<Message>) {
        messages.map { MessageDbo.from(it) }.also { dao.addMessages(it) }
    }

    override fun addMessages(messages: Collection<Message>, unread: Int) {
        messages.map { MessageDbo.from(it, unread = unread) }.also { dao.addMessages(it) }
    }

    override fun countChatMessages(): Single<Int> = dao.countChatMessages()

    override fun countChatMessages(chatId: String): Single<Int> = dao.countChatMessages(chatId)

    override fun countPeerMessages(): Single<Int> = dao.countPeerMessages()

    override fun countPeerMessages(peerId: String): Single<Int> = dao.countPeerMessages(peerId)

    override fun countUnreadMessages(): Single<Int> = dao.countUnreadMessages()

    override fun deleteMessages() = dao.deleteMessages()

    override fun deleteMessages(chatId: String) = dao.deleteMessages(chatId)

    override fun insertMessages(messages: Collection<Message>) {
        messages.map { MessageDbo.from(it) }.also { dao.insertMessages(it) }
    }

    override fun insertMessages(messages: Collection<Message>, unread: Int) {
        messages.map { MessageDbo.from(it, unread = unread) }.also { dao.insertMessages(it) }
    }

    override fun markMessagesAsRead(chatId: String): Int = dao.markMessagesAsRead(chatId)

    override fun messages(): Maybe<List<Message>> = dao.messages().map { it.mapList() }

    override fun messages(chatId: String): Maybe<List<Message>> = dao.messages(chatId).map { it.mapList() }
}
