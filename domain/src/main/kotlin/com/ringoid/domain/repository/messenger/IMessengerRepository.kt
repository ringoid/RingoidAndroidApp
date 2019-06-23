package com.ringoid.domain.repository.messenger

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface IMessengerRepository {

    fun getChat(chatId: String, resolution: ImageResolution, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Chat>

    fun getChatNew(chatId: String, resolution: ImageResolution, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Chat>

    fun pollChatNew(chatId: String, resolution: ImageResolution, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Flowable<Chat>

    // ------------------------------------------
    fun clearMessages(): Completable

    fun clearMessages(chatId: String): Completable

    fun getMessages(chatId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<List<Message>>

    fun sendMessage(essence: MessageEssence): Single<Message>
}
