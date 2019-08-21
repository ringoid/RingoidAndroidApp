package com.ringoid.repository.push

import com.ringoid.data.handleError
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager
import com.ringoid.domain.model.essence.push.PushTokenEssence
import com.ringoid.domain.model.essence.push.PushTokenEssenceUnauthorized
import com.ringoid.domain.repository.push.IPushRepository
import com.ringoid.repository.BaseRepository
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRepository @Inject constructor(
    cloud: IRingoidCloudFacade, spm: ISharedPrefsManager, aObjPool: IActionObjectPool)
    : BaseRepository(cloud, spm, aObjPool), IPushRepository {

    override fun updatePushToken(essence: PushTokenEssenceUnauthorized, dontWarn: Boolean): Completable =
        spm.accessSingle(dontWarn) { cloud.updatePushToken(PushTokenEssence.from(essence, it.accessToken)) }
            .handleError(tag = "updatePushToken", traceTag = "push/update_token")
            .ignoreElement()  // convert to Completable
}
