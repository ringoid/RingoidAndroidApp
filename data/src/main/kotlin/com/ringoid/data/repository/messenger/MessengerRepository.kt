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
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.actions.MessageActionObject
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.print
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.utility.randomString
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import timber.log.Timber
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(
    private val local: MessageDao, @PerUser private val sentMessagesLocal: MessageDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    private val mutex = Semaphore(1)
    private val sentMessageLocalReadersCount = AtomicInteger(0)
    private val sentMessagesLocalWriterLock = Semaphore(1)

    override fun getChat(chatId: String, resolution: ImageResolution, sourceFeed: String): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatOnly(chatId, resolution, lastActionTime = it, sourceFeed = sourceFeed) }

    private fun getChatOnly(chatId: String, resolution: ImageResolution, lastActionTime: Long, sourceFeed: String): Single<Chat> =
        spm.accessSingle {
            getChatImpl(it.accessToken, chatId, resolution, lastActionTime)
                .cacheMessagesFromChat(sourceFeed)
        }

    override fun getChatNew(chatId: String, resolution: ImageResolution, sourceFeed: String): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatNewOnly(chatId, resolution, lastActionTime = it, sourceFeed = sourceFeed) }

    private fun getChatNewOnly(chatId: String, resolution: ImageResolution, lastActionTime: Long, sourceFeed: String): Single<Chat> =
        spm.accessSingle {
            getChatImpl(it.accessToken, chatId, resolution, lastActionTime)
                .filterOutChatOldMessages(chatId, sourceFeed)
//                .concatWithUnconsumedSentLocalMessages(chatId, sourceFeed)
                .cacheMessagesFromChat(sourceFeed)  // cache only new chat messages, including sent by current user (if any), because they've been uploaded
//                .clearCachedSentMessages()
        }

    private fun getChatImpl(accessToken: String, chatId: String, resolution: ImageResolution, lastActionTime: Long) =
        cloud.getChat(accessToken, resolution, chatId, lastActionTime)
            .handleError(tag = "getChat(peerId=$chatId,$resolution,lat=$lastActionTime)", traceTag = "feeds/chat")
            .map { it.chat.mapToChat() }

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

    /**
     * Compare old messages list to incoming messages list for chat given by [chatId] and retain only
     * new messages, that have appeared in chat data. These messages can also include messages sent
     * by the current user.
     */
    private fun Single<Chat>.filterOutChatOldMessages(chatId: String, sourceFeed: String): Single<Chat> =
        toObservable()
        .withLatestFrom(local.messages(chatId = chatId, sourceFeed = sourceFeed).toObservable(),
            BiFunction { chat: Chat, localMessages: List<MessageDbo> ->
                Timber.v("[${Thread.currentThread().name}] Old messages ${localMessages.print()}")
                if (chat.messages.size > localMessages.size) {
                    val newMessages = chat.messages.subList(localMessages.size, chat.messages.size)
                    chat.copyWith(newMessages)  // retain only new messages
                } else chat.copyWith(messages = emptyList())  // no new messages
            })
        .flatMap { chat ->
            //region mutex
            mutex.acquireUninterruptibly()
            if (sentMessageLocalReadersCount.incrementAndGet() == 1) {
                sentMessagesLocalWriterLock.acquireUninterruptibly()
            }
            mutex.release()
            //endregion

            val unconsumedSentMessages = mutableListOf<Message>().apply { addAll(sentMessageLocalIds) }
            chat.messages.forEach { message ->
                if (message.isUserMessage()) {
                    unconsumedSentMessages.removeAll { it.id == message.clientId || it.clientId == message.clientId }
                }
            }
            chat.unconsumedSentLocalMessages.addAll(unconsumedSentMessages)
            sentMessageLocalIds.retainAll(unconsumedSentMessages)

            //region mutex
            mutex.acquireUninterruptibly()
            if (sentMessageLocalReadersCount.decrementAndGet() == 0) {
                sentMessagesLocalWriterLock.release()
            }
            mutex.release()
            //endregion

            Observable.just(chat)
        }
        .singleOrError()

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
        val sentMessage = Message(
            id = "_${randomString()}_${essence.peerId}",  // client-side id
            chatId = essence.peerId,
            /** 'clientId' equals to 'id' */
            peerId = DomainUtil.CURRENT_USER_ID,
            text = essence.text)

        val sourceFeed = essence.aObjEssence?.sourceFeed ?: ""
        val aobj = MessageActionObject(
            clientId = sentMessage.clientId,
            text = essence.text,
            sourceFeed = sourceFeed,
            targetImageId = essence.aObjEssence?.targetImageId ?: DomainUtil.BAD_ID,
            targetUserId = essence.aObjEssence?.targetUserId ?: DomainUtil.BAD_ID)

        return Completable.fromCallable { sentMessagesLocal.addMessage(MessageDbo.from(sentMessage, sourceFeed = sourceFeed)) }
            .doOnSubscribe {
                sentMessagesLocalWriterLock.acquireUninterruptibly()
                sentMessageLocalIds.add(sentMessage)
                aObjPool.put(aobj)
                sentMessagesLocalWriterLock.release()
            }
            .toSingleDefault(sentMessage)
    }

    private val sentMessageLocalIds = mutableSetOf<Message>()
}
