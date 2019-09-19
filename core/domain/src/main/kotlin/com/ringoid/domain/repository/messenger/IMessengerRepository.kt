package com.ringoid.domain.repository.messenger

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface IMessengerRepository {

    fun getChat(chatId: String, resolution: ImageResolution, isChatOpen: Boolean = false): Single<Chat>

    fun getChatOnly(chatId: String, resolution: ImageResolution, isChatOpen: Boolean = false): Single<Chat>

    fun getChatNew(chatId: String, resolution: ImageResolution, isChatOpen: Boolean = false): Single<Chat>

    fun pollChatNew(chatId: String, resolution: ImageResolution, isChatOpen: Boolean = false): Flowable<Chat>

    // ------------------------------------------
    fun clearMessages(): Completable

    fun clearMessages(chatId: String): Completable

    fun clearSentMessages(): Completable

    fun clearSentMessages(chatId: String): Completable

    fun getMessages(chatId: String): Single<List<Message>>

    fun sendMessage(essence: MessageEssence): Single<Message>

    fun fixSentLocalMessagesCache(chatId: String, unconsumedClientIds: List<String>): Completable
}
