package com.ringoid.repository.messenger

import com.ringoid.data.handleError
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.di.PerUser
import com.ringoid.datainterface.local.messenger.IMessageDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.actions.MessageActionObject
import com.ringoid.domain.model.actions.ReadMessageActionObject
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.messenger.Chat
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.model.messenger.MessageReadStatus
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.report.exception.SkipThisTryException
import com.ringoid.repository.BaseRepository
import com.ringoid.utility.randomString
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read and Write operations on messages local cache are guarded with semaphore, one for each distinguish
 * 'chatId'. Thus it's allowed to Read for one 'chatId' and Write for another 'chatId' at the same time
 * in the same messages local cache (database). If any Read or Write attempt fails to acquire lock,
 * it then terminates with [SkipThisTryException].
 *
 * Special cases are [pollChatNew] and [getMessages].
 *
 * ----------------------------------------------
 * Case [getMessages]:
 *
 * Given 'chatId', retrieves messages only from the local cache, no remote calls performed.
 * As usual, if Read for that particular 'chatId' is restricted at the moment of call [getMessages],
 * it's internal implementation [getMessagesImpl] will terminate with [SkipThisTryException] and then
 * [getMessages] will retry after short delay.
 *
 * Here is the special case, when Write messages for that 'chatId' is performing while [getMessages]
 * is called. This normally happens when push notification for that 'chatId' comes and it's handled
 * actually requests to update messages local cache, doing Write operation to it. In this case
 * [getMessages] have to wait for short delay and retry, as described above, and when it succeeds,
 * it will return the most actual messages from the local cache, being written there from push notification.
 * While [getMessages] is waiting for the local cache to be released, client code can experience some
 * visual delay and should handle it properly, showing some loading state.
 *
 * Actually, such external Write request, possibly from push notification handler,
 * will normally call [getChat] and either acquire lock for Write or terminate with
 * [SkipThisTryException].
 *
 * ----------------------------------------------
 * Case [pollChatNew]:
 *
 * Given 'chatId', fetches messages from the remote cloud only, also setting
 * [pollingDelay] value to perform next fetch after that delay. Then it repeats fetching with that
 * delay, which is updated every time. Once data is fetched, Write to the local cache is then performed.
 * As usual, if Write for that particular 'chatId' is restricted at the moment of Write to the local
 * cache operation call, internals of [pollChatNew] will terminate with [SkipThisTryException] and
 * then [pollChatNew] will retry (instead of repeat) it's attempt to fetch the remote cloud and cache
 * the result locally again in [pollingDelay] time passed.
 *
 * If some external Write request, such as push notification handler, has occurred while polling chat,
 * it will normally call [getChat] and either acquire lock for Write or terminate with
 * [SkipThisTryException].
 */
@Singleton
class MessengerRepository @Inject constructor(
    private val local: IMessageDbFacade,
    @PerUser private val sentMessagesLocal: IMessageDbFacade,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    private val sentMessages = ConcurrentHashMap<String, MutableSet<Message>>()
    private val semaphores = mutableMapOf<String, Semaphore>()
    private var pollingDelay = 5000L  // in ms

    init {
        restoreCachedSentMessagesLocal()
    }

    /* Concurrency */
    // --------------------------------------------------------------------------------------------
    @Synchronized
    private fun tryAcquireLock(chatId: String): Boolean {
        if (!semaphores.contains(chatId)) {
            semaphores[chatId] = Semaphore(1)  // mutex
        }
        return semaphores[chatId]!!.tryAcquire()
    }

    @Synchronized
    private fun releaseLock(chatId: String) {
        semaphores[chatId]?.release()
    }

    // --------------------------------------------------------------------------------------------
    override fun getChat(chatId: String, resolution: ImageResolution, isChatOpen: Boolean): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatOnly(chatId, resolution, isChatOpen, lastActionTime = it) }

    override fun getChatOnly(chatId: String, resolution: ImageResolution, isChatOpen: Boolean): Single<Chat> =
        getChatOnly(chatId, resolution, isChatOpen, aObjPool.lastActionTime())

    override fun getChatNew(chatId: String, resolution: ImageResolution, isChatOpen: Boolean): Single<Chat> =
        aObjPool.triggerSource().flatMap { getChatNewOnly(chatId, resolution, isChatOpen, lastActionTime = it) }

    override fun pollChatNew(chatId: String, resolution: ImageResolution, isChatOpen: Boolean): Flowable<Chat> =
        getChatNew(chatId, resolution, isChatOpen)
            .repeatWhen { it.flatMap { Flowable.timer(pollingDelay, TimeUnit.MILLISECONDS, Schedulers.io()) } }
            .retryWhen { errorSource ->
                errorSource.flatMap { error ->
                    if (error is SkipThisTryException) {
                        // delay resubscription by 'pollingDelay' and continue polling
                        Timber.w("Skip current iteration and continue polling later on in $pollingDelay ms")
                        Flowable.timer(pollingDelay, TimeUnit.MILLISECONDS, Schedulers.io())
                    } else {
                        Flowable.error(error)
                    }
                }
            }

    // ------------------------------------------
    private fun getChatOnly(chatId: String, resolution: ImageResolution,
                            isChatOpen: Boolean, lastActionTime: Long): Single<Chat> =
        spm.accessSingle { accessToken ->
            Single.just(0L)
                .flatMap {
                    if (tryAcquireLock(chatId)) {
                        getChatImpl(accessToken.accessToken, chatId, resolution, lastActionTime)
                            .concatWithUnconsumedSentLocalMessages(chatId)
                            .cacheUnconsumedSentLocalMessages(chatId)
                            .cacheMessagesFromChat()
                            .readMessagesFromPeerByUser(isChatOpen)
                            .doOnSuccess { Timber.v("New chat full: ${it.print()}") }
                            .doFinally { releaseLock(chatId) }
                    } else {
                        Timber.w("Skip current iteration")
                        Single.error(SkipThisTryException())
                    }
                }
        }

    private fun getChatNewOnly(chatId: String, resolution: ImageResolution,
                               isChatOpen: Boolean, lastActionTime: Long): Single<Chat> =
        spm.accessSingle { accessToken ->
            Single.just(0L)
                .flatMap {
                    if (tryAcquireLock(chatId)) {
                        getChatImpl(accessToken.accessToken, chatId, resolution, lastActionTime)
                            .filterOutChatOldMessages(chatId)
                            .concatWithUnconsumedSentLocalMessages(chatId)
                            .cacheUnconsumedSentLocalMessages(chatId)
                            .cacheMessagesFromChat()  // cache only new chat messages, including sent by current user (if any), because they've been uploaded
                            .readMessagesFromPeerByUser(isChatOpen)
                            .doOnSuccess { Timber.v("New chat delta: ${it.print()}") }
                            .doFinally { releaseLock(chatId) }
                    } else {
                        Timber.w("Skip current iteration")
                        Single.error(SkipThisTryException())
                    }
                }
        }

    private fun getChatImpl(accessToken: String, chatId: String, resolution: ImageResolution, lastActionTime: Long) =
        cloud.getChat(accessToken, resolution, chatId, lastActionTime)
            .handleError(tag = "getChat(peerId=$chatId,$resolution,lat=$lastActionTime)", traceTag = "feeds/chat")
            .doOnSuccess { chat ->
                if (chat.pullAgainAfter >= 500L) {
                    pollingDelay = chat.pullAgainAfter  // update polling delay from response data
                }
            }
            .map { it.chat.mapToChat() }

    // ------------------------------------------
    private fun Single<Chat>.cacheMessagesFromChat(): Single<Chat> =
        flatMap { Completable.fromAction { local.insertMessages(it.messages) }.toSingleDefault(it) }

    /**
     * Compare old messages list to incoming messages list for chat given by [chatId] and retain only
     * new messages, that have appeared in chat data. These messages can also include messages sent
     * by the current user.
     */
    private fun Single<Chat>.filterOutChatOldMessages(chatId: String): Single<Chat> =
        toObservable()
        .withLatestFrom(local.countChatMessages(chatId = chatId).toObservable(),
            BiFunction { chat: Chat, localMessagesCount: Int ->
                if (chat.messages.size > localMessagesCount) {
                    val newMessages = chat.messages.subList(localMessagesCount, chat.messages.size)
                    chat.copyWith(newMessages)  // retain only new messages
                } else chat.copyWith(messages = emptyList())  // no new messages
            })
        .singleOrError()

    /**
     * Given the most recent sublist of chat's messages, analyze locally stored sent messages and
     * retain only unconsumed ones, i.e. those sent messages that don't present in chat.
     *
     * @note N-Readers-1-Writer pattern is used to concurrently access locally stored sent messages.
     */
    private fun Single<Chat>.concatWithUnconsumedSentLocalMessages(chatId: String): Single<Chat> =
        map { chat ->
            if (sentMessages.containsKey(chatId)) {
                chat.messages.forEach { message ->
                    if (message.isUserMessage()) {
                        sentMessages[chatId]!!.removeAll { it.id == message.clientId || it.clientId == message.clientId }
                    }
                }
                // retain unconsumed sent local messages only
                val unconsumedSentMessages = mutableListOf<Message>()
                    .apply { addAll(sentMessages[chatId]!!) }  // order can change here

                chat.unconsumedSentLocalMessages.addAll(unconsumedSentMessages.sortedBy { it.ts })
            }
            chat  // result value
        }

    private fun Single<Chat>.cacheUnconsumedSentLocalMessages(chatId: String): Single<Chat> =
        flatMap { chat ->
            sentMessagesLocal.countChatMessages(chatId).map { count -> count to chat }
        }
        .flatMap { (count, chat) ->
            if (count > 0) {
                Completable.fromCallable { sentMessagesLocal.deleteMessages(chatId) }
                           .toSingleDefault(chat)
            } else Single.just(chat)
        }
        .flatMap { chat ->
            if (sentMessages.containsKey(chatId) && sentMessages[chatId]!!.isNotEmpty()) {
                Completable.fromCallable { sentMessagesLocal.addMessages(sentMessages[chatId]!!) }
                           .toSingleDefault(chat)
            } else Single.just(chat)
        }

    private fun Single<Chat>.readMessagesFromPeerByUser(isChatOpen: Boolean): Single<Chat> =
        flatMap { chat ->
            if (isChatOpen) {
                chat.messages
                    .filter { it.isPeerMessage() && it.readStatus == MessageReadStatus.UnreadByUser }
                    .takeIf { it.isNotEmpty() }
                    ?.let { Maybe.just(it) }
                    ?.readMessagesFromPeerByUser(chatId = chat.id)
                    ?.toSingleDefault(chat)
                    ?: Single.just(chat)
            } else {
                Single.just(chat)
            }
        }

    // --------------------------------------------------------------------------------------------
    override fun clearMessages(): Completable =
        Completable.fromCallable { local.deleteMessages() }
                   .andThen(clearSentMessages())

    override fun clearMessages(chatId: String): Completable =
        Completable.fromCallable { local.deleteMessages(chatId) }
                   .andThen(clearSentMessages(chatId))

    override fun clearSentMessages(): Completable =
        Completable.fromCallable {
            sentMessagesLocal.deleteMessages()
            sentMessages.clear()
        }

    override fun clearSentMessages(chatId: String): Completable =
        Completable.fromCallable {
            sentMessagesLocal.deleteMessages(chatId)
            sentMessages[chatId]?.clear()
        }

    // ------------------------------------------
    // messages cached since last network request + sent user messages (cache locally)
    /**
     * Retrieves messages from the local cache only.
     */
    override fun getMessages(chatId: String): Single<List<Message>> =
        getMessagesOnly(chatId)
            .retryWhen { errorSource ->
                errorSource.flatMap { error ->
                    if (error is SkipThisTryException) {
                        Flowable.timer(200, TimeUnit.MILLISECONDS, Schedulers.io())
                    } else {
                        Flowable.error(error)
                    }
                }
            }

    private fun getMessagesOnly(chatId: String): Single<List<Message>> =
        Single.just(0L)
            .flatMap {
                if (tryAcquireLock(chatId)) {
                    getMessagesAndMarkAsReadByUser(chatId).doFinally { releaseLock(chatId) }
                } else {
                    Timber.w("Cache is busy, retry get local messages")
                    Single.error(SkipThisTryException())
                }
            }

    private fun getMessagesImpl(chatId: String): Single<List<Message>> =
        local.messages(chatId = chatId)
            .concatWith(sentMessagesLocal.messages(chatId))
            .collect({ mutableListOf<Message>() }, { out, localMessages -> out.addAll(localMessages) })
            .map { it.reversed() }

    /**
     * Retrieves local messages for chat, given by [chatId], and also marks unread by the current user
     * ones as read ones, composing and committing action objects for that. Run these tasks in parallel.
     */
    private fun getMessagesAndMarkAsReadByUser(chatId: String): Single<List<Message>> =
        getMessagesImpl(chatId)
            .mergeWith(readMessagesFromPeer(chatId).toSingleDefault(emptyList()))
            .parallel(2)
            .runOn(Schedulers.io())
            .sequential()
            .filter { it.isNotEmpty() }
            .singleOrError()

    // ------------------------------------------
    override fun sendMessage(essence: MessageEssence): Single<Message> {
        val sentMessage = Message(
            id = "_${randomString()}_${essence.peerId}",  // client-side id
            chatId = essence.peerId,
            /** 'clientId' equals to 'id' */
            peerId = DomainUtil.CURRENT_USER_ID,  // message sent by the current user
            readStatus = MessageReadStatus.UnreadByPeer,  // just sent message is unread by peer
            text = essence.text,
            ts = System.currentTimeMillis())  // ts at sending message

        val sourceFeed = essence.aObjEssence?.sourceFeed ?: ""
        val aobj = MessageActionObject(
            clientId = sentMessage.clientId,
            text = essence.text,
            sourceFeed = sourceFeed,
            targetImageId = essence.aObjEssence?.targetImageId ?: DomainUtil.BAD_ID,
            targetUserId = essence.aObjEssence?.targetUserId ?: DomainUtil.BAD_ID)

        return Completable.fromCallable { sentMessagesLocal.addMessage(sentMessage) }
            .doOnSubscribe {
                keepSentMessage(sentMessage)
                aObjPool.put(aobj)  // immediate action object will be committed right now
            }
            .toSingleDefault(sentMessage)
    }

    // ------------------------------------------
    override fun fixSentLocalMessagesCache(chatId: String, unconsumedClientIds: List<String>): Completable =
        Completable.fromAction {
            if (sentMessages.containsKey(chatId)) {
                sentMessages[chatId]!!.retainAll { it.clientId in unconsumedClientIds }
            }
        }

    /**
     * Given only messages from peer that are unread by user for chat, given by [chatId],
     * make them all read by user remotely and locally.
     *
     * @see [readMessagesFromPeerByUser].
     */
    private fun readMessagesFromPeer(chatId: String): Completable =
        local.messagesPeer(chatId = chatId, readStatus = MessageReadStatus.UnreadByUser)
             .doOnSubscribe { Timber.v("Start reading messages from peer by current user, for chat: $chatId") }
             .readMessagesFromPeerByUser(chatId)

    /**
     * Composes [ReadMessageActionObject] action objects for each message from peer
     * in the chat, given by [chatId], and commit them to the remote cloud to mark
     * these messages as 'read' by the current user for the peer in that chat.
     *
     * Input messages should be preliminary filtered by belonging to peer and having read status
     * as unread by the current user.
     *
     * @note: [chatId] is equal to 'peerId', since only peer's messages are retrieved here.
     */
    private fun Maybe<List<Message>>.readMessagesFromPeerByUser(chatId: String): Completable =
        map { it.map { message ->
            ReadMessageActionObject(messageId = message.id, peerId = message.chatId)
        } }
        .flatMapCompletable(aObjPool::putSource)
        .andThen(aObjPool.triggerSource().ignoreElement())
        .andThen(Completable.fromAction { local.markMessagesAsReadByUser(chatId = chatId) })

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    private fun restoreCachedSentMessagesLocal() {
        sentMessagesLocal.messages()
            .subscribeOn(Schedulers.io())
            .subscribe({ it.forEach { message -> keepSentMessage(message) } }, Timber::e)
    }

    private fun keepSentMessage(sentMessage: Message) {
        if (!sentMessages.containsKey(sentMessage.chatId)) {
            sentMessages[sentMessage.chatId] = Collections.newSetFromMap(ConcurrentHashMap())
        }
        sentMessages[sentMessage.chatId]!!.add(sentMessage)  // will be sorted by ts
    }
}
