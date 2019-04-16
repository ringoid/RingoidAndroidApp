package com.ringoid.data.action_storage

import com.ringoid.data.di.PerBackup
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.scope.UserScopeProvider
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistActionObjectPool @Inject constructor(
    cloud: RingoidCloud, @PerBackup private val backup: ActionObjectDao,
    private val local: ActionObjectDao, private val mapper: ActionObjectDboMapper,
    private val spm: SharedPrefsManager, private val userScopeProvider: UserScopeProvider)
    : BaseActionObjectPool(cloud) {

    // ------------------------------------------------------------------------
    override fun getTotalQueueSize(): Int = 0  // don't trigger by capacity hit

    // --------------------------------------------------------------------------------------------
    @Synchronized
    override fun put(aobj: OriginActionObject) {
        Timber.v("Put action object: $aobj")
        Single.fromCallable { local.addActionObject(mapper.map(aobj)) }
            .subscribeOn(Schedulers.io())
            .flatMap { local.countActionObjects() }
            .subscribe({ analyzeActionObject(aobj) }, Timber::e)
    }

    override fun trigger() {
    }

    override fun triggerSource(): Single<Long> {
    }

    override fun lastActionTime(): Long {
    }

    override fun finalizePool() {
    }
}
