package com.ringoid.datainterface.local.messenger

import com.ringoid.domain.model.messenger.Message
import io.reactivex.Maybe
import io.reactivex.Single

interface IMessageDbFacade {

    fun addMessage(message: Message)

    fun addMessages(messages: Collection<Message>)

    fun addMessages(messages: Collection<Message>, unread: Int)

    fun countChatMessages(): Single<Int>

    fun countChatMessages(chatId: String): Single<Int>

    fun countPeerMessages(): Single<Int>

    fun countPeerMessages(peerId: String): Single<Int>

    fun countUnreadMessages(): Single<Int>

    fun deleteMessages()

    fun deleteMessages(chatId: String)

    fun insertMessages(messages: Collection<Message>)

    fun insertMessages(messages: Collection<Message>, unread: Int)

    fun markMessagesAsRead(chatId: String): Int

    fun messages(): Maybe<List<Message>>

    fun messages(chatId: String): Maybe<List<Message>>
}
