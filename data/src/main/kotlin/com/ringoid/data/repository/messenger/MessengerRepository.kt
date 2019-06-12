package com.ringoid.data.repository.messenger

import com.ringoid.data.di.PerUser
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.actions.MessageActionObject
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.utility.RemoveIf
import com.ringoid.utility.randomString
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(
    private val local: MessageDao, @PerUser private val sentMessagesLocal: MessageDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    override fun getChat(chatId: String, resolution: ImageResolution, sourceFeed: String): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatOnly(chatId, resolution, lastActionTime = it, sourceFeed = sourceFeed) }

    private fun getChatOnly(chatId: String, resolution: ImageResolution, lastActionTime: Long, sourceFeed: String): Single<Chat> =
        spm.accessSingle {
            getChatImpl(it.accessToken, chatId, resolution, lastActionTime)
                .cacheMessagesFromChat(sourceFeed)
                .clearCachedSentMessages()  // clear sent user messages because they are present in Chat data, that has just been cached
        }

    override fun getChatNew(chatId: String, resolution: ImageResolution, sourceFeed: String): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatNewOnly(chatId, resolution, lastActionTime = it, sourceFeed = sourceFeed) }

    private fun getChatNewOnly(chatId: String, resolution: ImageResolution, lastActionTime: Long, sourceFeed: String): Single<Chat> =
        spm.accessSingle {
            getChatImpl(it.accessToken, chatId, resolution, lastActionTime)
                .filterOutChatOldMessages(chatId, sourceFeed)
                .cacheMessagesFromChat(sourceFeed)  // cache only new chat messages
                .clearCachedSentMessages()  // clear sent user messages because they are present in Chat data, that has just been cached
        }

    private fun getChatImpl(accessToken: String, chatId: String, resolution: ImageResolution, lastActionTime: Long) =
        cloud.getChat(accessToken, resolution, chatId, lastActionTime)
            .handleError(tag = "getChat(peerId=$chatId,$resolution,lat=$lastActionTime)", traceTag = "feeds/chat")
            .doOnSuccess { DebugLogUtil.v("# Chat messages: [${it.chat.messages.size}] originally") }
            .map { it.chat.map() }

    // ------------------------------------------
    private fun Single<Chat>.cacheMessagesFromChat(sourceFeed: String): Single<Chat> =
        flatMap { chat ->
            val messages = mutableListOf<MessageDbo>()
                .apply { addAll(chat.messages.map { MessageDbo.from(it, sourceFeed) }) }
            Completable.fromCallable { local.insertMessages(messages) }
                .toSingleDefault(chat)
        }

    private fun Single<Chat>.clearCachedSentMessages(): Single<Chat> =
        doOnSuccess { sentMessagesLocal.deleteMessages() }

    private fun Single<Chat>.filterOutChatOldMessages(chatId: String, sourceFeed: String): Single<Chat> =
        toObservable()
        .withLatestFrom(local.messages(chatId = chatId, sourceFeed = sourceFeed).toObservable(),
            BiFunction { chat: Chat, localMessages: List<MessageDbo> ->
                if (chat.messages.size > localMessages.size) {
                    val newMessages = chat.messages.subList(localMessages.size, chat.messages.size)
                    chat.copyWith(newMessages)  // retain only new messages
                } else chat.copyWith(messages = emptyList())  // no new messages
            })
        .withLatestFrom(sentMessagesLocal.messages(chatId = chatId, sourceFeed = sourceFeed).toObservable(),
            BiFunction { chat: Chat, sentLocalMessages: List<MessageDbo> ->
                sentLocalMessages.forEach { message ->
                    chat.messages.RemoveIf { it.isUserMessage() && it.text == message.text }
                }
                chat
            })
        .singleOrError()
        .doOnSuccess { DebugLogUtil.v("# Chat messages: [${it.messages.size}] after filtering out cached (old) messages") }

    // --------------------------------------------------------------------------------------------
    override fun clearMessages(): Completable =
        Completable.fromCallable {
            local.deleteMessages()
            sentMessagesLocal.deleteMessages()
        }

    override fun clearMessages(chatId: String): Completable =
        Completable.fromCallable {
            local.deleteMessages(chatId)
            sentMessagesLocal.deleteMessages(chatId)
        }

    // messages cached since last network request + sent user messages (cache locally)
    override fun getMessages(chatId: String, sourceFeed: String): Single<List<Message>> =
        Maybe.fromCallable { local.markMessagesAsRead(chatId = chatId, sourceFeed = sourceFeed) }
            .flatMap { local.messages(chatId = chatId, sourceFeed = sourceFeed) }
            .concatWith(sentMessagesLocal.messages(chatId))
            .collect({ mutableListOf<MessageDbo>() }, { out, localMessages -> out.addAll(localMessages) })
            .map { it.mapList().reversed() }

    override fun sendMessage(essence: MessageEssence): Single<Message> {
        aObjPool.put(MessageActionObject(text = essence.text, sourceFeed = essence.aObjEssence?.sourceFeed ?: "",
            targetImageId = essence.aObjEssence?.targetImageId ?: DomainUtil.BAD_ID, targetUserId = essence.aObjEssence?.targetUserId ?: DomainUtil.BAD_ID))
        val sentMessage = Message(id = "${essence.peerId}_${randomString()}", chatId = essence.peerId, peerId = DomainUtil.CURRENT_USER_ID, text = essence.text)
        return Single.just(sentMessage).cacheSentMessage()
    }

    // ------------------------------------------
    private fun Single<Message>.cacheSentMessage(): Single<Message> =
        doOnSuccess { sentMessagesLocal.addMessage(MessageDbo.from(it)) }
}
