package com.ringoid.data.repository.messenger

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.domain.model.actions.MessageActionObject
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(private val local: MessageDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool) : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    override fun getMessages(peerId: String): Single<List<Message>> =
            local.messages(peerId).map { it.mapList() }

    override fun sendMessage(essence: MessageEssence): Single<Message> {
        aObjPool.put(MessageActionObject(text = essence.text, sourceFeed = essence.aObjEssence?.sourceFeed ?: "",
            targetImageId = essence.aObjEssence?.targetImageId ?: BAD_ID, targetUserId = essence.aObjEssence?.targetUserId ?: BAD_ID))
        return Single.just(Message(chatId = essence.peerId, peerId = DomainUtil.CURRENT_USER_ID, text = essence.text))
    }
}
