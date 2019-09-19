package com.ringoid.data.local.database.facade.messenger

import com.ringoid.config.AppMigrationFrom
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.datainterface.local.messenger.IMessageDbFacade
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.messenger.MessageReadStatus
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDbFacadeImpl @Inject constructor(private val dao: MessageDao) : IMessageDbFacade {

    override fun addMessage(message: Message) {
        MessageDbo.from(message).also { dao.addMessage(it) }
    }

    override fun addMessages(messages: Collection<Message>) {
        messages.map { MessageDbo.from(it) }.also { dao.addMessages(it) }
    }

    override fun countChatMessages(): Single<Int> = dao.countChatMessages()

    override fun countChatMessages(chatId: String): Single<Int> = dao.countChatMessages(chatId)

    override fun countPeerMessages(): Single<Int> = dao.countPeerMessages()

    override fun countPeerMessages(chatId: String): Single<Int> = dao.countPeerMessages(chatId)

    override fun countUserMessages(): Single<Int> = dao.countUserMessages()

    override fun countUserMessages(chatId: String): Single<Int> = dao.countUserMessages(chatId)

    override fun countUnreadByUserMessages(): Single<Int> = dao.countUnreadByUserMessages()

    override fun deleteMessages() = dao.deleteMessages()

    override fun deleteMessages(chatId: String) = dao.deleteMessages(chatId)

    override fun insertMessages(messages: Collection<Message>) {
        messages.map { MessageDbo.from(it) }.also { dao.insertMessages(it) }
    }

    override fun markMessagesAsReadByUser(chatId: String): Int = dao.markMessagesAsReadByUser(chatId)

    override fun messages(): Maybe<List<Message>> = dao.messages().map { it.mapList() }

    override fun messages(chatId: String): Maybe<List<Message>> = dao.messages(chatId).map { it.mapList() }

    override fun messages(chatId: String, peerId: String, readStatus: MessageReadStatus): Maybe<List<Message>> {
        checkConsistencyForPeerIdAndReadStatus(peerId = peerId, readStatus = readStatus)
        return dao.messages(chatId = chatId, peerId = peerId, readStatus = readStatus.value).map { it.mapList() }
    }

    override fun messagesPeer(chatId: String, readStatus: MessageReadStatus): Maybe<List<Message>> {
        // [chatId] is normally equal to [peerId], but [peerId] could be equal to [DomainUtil.CURRENT_USER_ID]
        checkConsistencyForPeerIdAndReadStatus(peerId = chatId, readStatus = readStatus)
        return dao.messagesPeer(chatId = chatId, readStatus = readStatus.value).map { it.mapList() }
    }

    override fun messagesUser(chatId: String, readStatus: MessageReadStatus): Maybe<List<Message>> {
        checkConsistencyForPeerIdAndReadStatus(peerId = DomainUtil.CURRENT_USER_ID, readStatus = readStatus)
        return dao.messagesUser(chatId = chatId, readStatus = readStatus.value).map { it.mapList() }
    }

    // ------------------------------------------
    private fun checkConsistencyForPeerIdAndReadStatus(peerId: String, readStatus: MessageReadStatus) {
        when (peerId) {
            DomainUtil.CURRENT_USER_ID ->
                when (readStatus) {
                    MessageReadStatus.ReadByUser,
                    MessageReadStatus.UnreadByUser -> throw IllegalArgumentException("Read status $readStatus can't be used for the current user")
                    else -> { /* no-op */ }
                }
            else ->
                when (readStatus) {
                    MessageReadStatus.ReadByPeer,
                    MessageReadStatus.UnreadByPeer -> throw IllegalArgumentException("Read status $readStatus can't be used for peer: $peerId")
                    else -> { /* no-op */ }
                }
        }
    }

    // App migration
    // --------------------------------------------------------------------------------------------
    @AppMigrationFrom(version = 255)
    override fun migrateMarkAllUserMessagesAsReadByPeer() {
        dao.migrateMarkAllUserMessagesAsReadByPeer()
    }
}
