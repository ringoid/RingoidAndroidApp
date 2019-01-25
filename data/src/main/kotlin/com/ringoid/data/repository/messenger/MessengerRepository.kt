package com.ringoid.data.repository.messenger

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.database.dao.messenger.MessageDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.essence.messenger.MessageEssence
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(
    private val local: MessageDao, cloud: RingoidCloud,
    spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IMessengerRepository {

    override fun getMessages(peerId: String): Single<List<Message>> =
            local.messages(peerId).map { it.mapList() }

    override fun sendMessage(essence: MessageEssence): Single<Message> {
        // TODO: send message to Cloud
        return Single.just(Message(peerId = DomainUtil.CURRENT_USER_ID, text = essence.text))
    }
}
