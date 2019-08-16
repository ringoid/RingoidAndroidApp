package com.ringoid.repository.system

import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.ISystemCloudFacade
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.repository.system.ISystemRepository
import com.ringoid.repository.BaseRepository
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val systemCloud: ISystemCloudFacade,
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), ISystemRepository {

    override fun postToSlack(channelId: String, text: String): Completable =
        systemCloud.postToSlack(channelId = channelId, text = text)
}
