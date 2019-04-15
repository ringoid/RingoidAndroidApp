package com.ringoid.data.repository.messenger

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.di.PerUser
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.model.actions.MessageActionObject
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.messenger.IMessengerRepository
import com.ringoid.utility.randomString
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(
    private val local: MessageDao, @PerUser private val sentMessagesLocal: MessageDao,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    override fun clearMessages(): Completable =
        Completable.fromCallable {
            local.deleteMessages()
            sentMessagesLocal.deleteMessages()
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
