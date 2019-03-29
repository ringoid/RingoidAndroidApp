package com.ringoid.data.repository.push

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.repository.BaseRepository
import com.ringoid.data.repository.handleError
import com.ringoid.domain.model.essence.push.PushTokenEssence
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.repository.ISharedPrefsManager
import com.ringoid.domain.repository.push.IPushRepository
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRepository @Inject constructor(
    cloud: RingoidCloud, spm: ISharedPrefsManager, aObjPool: ActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IPushRepository {

    override fun updatePushToken(essence: PushTokenEssenceUnauthorized): Completable =
        spm.accessSingle { cloud.updatePushToken(PushTokenEssence.from(essence, it.accessToken)) }
        .handleError()
        .ignoreElement()  // convert to Completable
}
