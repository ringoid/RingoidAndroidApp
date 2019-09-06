package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import io.reactivex.Single

abstract class DelayQueueActionObjectPool(cloud: IRingoidCloudFacade, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Thread performs [triggerSourceImpl], others are delayed until the first one finishes.
     * Delays thread for a short period of time, then check whether the job is still in progress,
     * and if so - delay again, otherwise - start doing the job.
     */
    override fun triggerSource(): Single<Long> = Single.just(0L)
}
