package com.ringoid.data.action_storage.di

import com.ringoid.data.action_storage.BaseActionObjectPool
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import io.reactivex.Single

abstract class NonBlockingActionObjectPool(cloud: IRingoidCloudFacade, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    protected abstract fun triggerSourceImpl(): Single<Long>

    override fun triggerSource(): Single<Long> = triggerSourceImpl()
}
