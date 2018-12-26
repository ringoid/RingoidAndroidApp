package com.ringoid.data.repository.messenger

import com.ringoid.data.local.SharedPrefsManager
import com.ringoid.data.local.dao.messenger.MessageDao
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.model.mapList
import com.ringoid.domain.model.messenger.Message
import com.ringoid.domain.repository.messenger.IMessengerRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessengerRepository @Inject constructor(
    private val cloud: RingoidCloud, private val local: MessageDao,
    private val spm: SharedPrefsManager) : IMessengerRepository {

    // TODO: get rid of 'single()'
    override fun getMessages(peerId: String): Single<List<Message>> =
            local.messages(peerId).single(emptyList()).map { it.mapList() }
}
