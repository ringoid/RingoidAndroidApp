package com.ringoid.repository.system

import com.ringoid.data.remote.api.RingoidCloud
import com.ringoid.data.remote.api.SystemCloud
import com.ringoid.repository.BaseRepository
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.repository.system.ISystemRepository
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val systemCloud: SystemCloud,
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), ISystemRepository {

    override fun postToSlack(channelId: String, text: String): Completable =
        systemCloud.postToSlack(channelId = channelId, text = text)
}
