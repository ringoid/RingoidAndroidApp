package com.ringoid.datainterface.local.messenger

import com.ringoid.config.AppMigrationFrom
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.messenger.MessageReadStatus
import com.ringoid.utility.DebugOnly
import io.reactivex.Maybe
import io.reactivex.Single

interface IMessageDbFacade {

    fun addMessage(message: Message)

    fun addMessages(messages: Collection<Message>)

    fun countChatMessages(): Single<Int>

    fun countChatMessages(chatId: String): Single<Int>

    fun countPeerMessages(): Single<Int>

    fun countPeerMessages(chatId: String): Single<Int>

    fun countUserMessages(): Single<Int>

    fun countUserMessages(chatId: String): Single<Int>

    fun countUnreadByUserMessages(): Single<Int>

    fun deleteMessages()

    fun deleteMessages(chatId: String)

    fun deleteMessages(messages: Collection<Message>)

    fun insertMessages(messages: Collection<Message>)

    fun lastMessage(chatId: String): Single<Message>

    fun markMessagesAsReadByUser(chatId: String): Int

    fun messages(): Maybe<List<Message>>

    fun messages(chatId: String): Maybe<List<Message>>

    fun messagesPeer(chatId: String): Maybe<List<Message>>

    fun messagesUser(chatId: String): Maybe<List<Message>>

    fun messages(chatId: String, peerId: String, readStatus: MessageReadStatus): Maybe<List<Message>>

    fun messagesPeer(chatId: String, readStatus: MessageReadStatus): Maybe<List<Message>>

    fun messagesUser(chatId: String, readStatus: MessageReadStatus): Maybe<List<Message>>

    fun updateMessages(messages: Collection<Message>): Int

    // App migration
    // --------------------------------------------------------------------------------------------
    @AppMigrationFrom(version = 255)
    fun migrateMarkAllUserMessagesAsReadByPeer()

    /* Debug */
    // --------------------------------------------------------------------------------------------
    @DebugOnly
    fun debugMarkPeerMessagesAsUnreadByUser(chatId: String)
}
