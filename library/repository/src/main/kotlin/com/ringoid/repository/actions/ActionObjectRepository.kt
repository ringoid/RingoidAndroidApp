package com.ringoid.repository.actions

import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.repository.actions.IActionObjectRepository
import com.ringoid.repository.BaseRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectRepository @Inject constructor(
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IActionObjectRepository {

    override fun countCachedActionObjects(): Single<Int> = aObjPool.countActionObjects()
}
