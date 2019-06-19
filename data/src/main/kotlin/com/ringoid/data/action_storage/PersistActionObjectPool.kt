package com.ringoid.data.action_storage

import com.ringoid.data.di.PerBackup
import com.ringoid.data.local.database.dao.action_storage.ActionObjectDao
import com.ringoid.data.local.database.model.action_storage.ActionObjectDboMapper
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.data.remote.model.actions.CommitActionsResponse
import com.ringoid.data.repository.handleError
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.SimulatedException
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistActionObjectPool @Inject constructor(
    cloud: RingoidCloud, @PerBackup private val backup: ActionObjectDao,
    private val local: ActionObjectDao, private val mapper: ActionObjectDboMapper,
    spm: SharedPrefsManager, private val userScopeProvider: UserScopeProvider)
    : BarrierActionObjectPool(cloud, spm) {

    // ------------------------------------------------------------------------
    override fun getTotalQueueSize(): Int = 0  // don't trigger by capacity hit

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    override fun put(aobj: OriginActionObject) {
        Timber.v("Put action object: $aobj")
        Completable.fromCallable { local.addActionObject(mapper.map(aobj)) }
            .subscribeOn(Schedulers.io())
            .autoDisposable(userScopeProvider)
            .subscribe({ analyzeActionObject(aobj) }, Timber::e)
    }

    @Suppress("CheckResult")
    override fun put(aobjs: Collection<OriginActionObject>) {
        Timber.v("Put actions object: ${aobjs.joinToString()}")
        Completable.fromCallable { local.addActionObjects(mapper.map(aobjs)) }
            .subscribeOn(Schedulers.io())
            .autoDisposable(userScopeProvider)
            .subscribe({ aobjs.forEach { analyzeActionObject(it) } }, Timber::e)
    }

    @Suppress("CheckResult")
    override fun trigger() {
        local.countActionObjects()
            .subscribeOn(Schedulers.newThread())
            .flatMap { count ->
                if (count <= 0) {
                    Single.just(0L)  // do nothing on empty queue
                } else {
                    triggerSource()
                }
            }
            .autoDisposable(userScopeProvider)
            .subscribe({ Timber.d("Trigger Queue finished, last action time: $it") }, Timber::e)
    }

    override fun triggerSourceImpl(): Single<Long> =
        local.countActionObjects()
            .flatMap { count ->
                if (DomainUtil.withSimulatedError()) {
                    DomainUtil.simulateError()
                    Single.error<CommitActionsResponse>(SimulatedException())
                } else
                if (count <= 0) {
                    Timber.v("Nothing to commit (no actions)")
                    Single.just(CommitActionsResponse(lastActionTime()))  // do nothing on empty queue
                } else {
                    local.actionObjects()
                        .flatMap {
                            Completable.fromCallable { local.markActionObjectsAsUsed(ids = it.map { it.id }) }
                                       .toSingleDefault(it)
                        }
                        .map { it.map { it.map() } }  // map from dbo to domain model
                        .flatMap { queue ->
                            spm.accessSingle {
                                /**
                                 * @see [ActionObjectPool.triggerSourceImpl] for explanation.
                                 */
                                val queueCopy = ArrayDeque(queue)
                                val essence = CommitActionsEssence(it.accessToken, queueCopy)
                                cloud.commitActions(essence)
                        }
                    }
                }
            }
            .doOnError {
                SentryUtil.breadcrumb("Commit actions error",
                    "exception" to "${it.javaClass}", "message" to "${it.message}")
                DebugLogUtil.e("Commit actions error: $it")
            }
            .handleError(tag = "commitActions", traceTag = "actions/actions")
            .doOnSubscribe { dropStrategyData() }
            .doOnSuccess { updateLastActionTime(it.lastActionTime) }
            .doOnDispose {
                DebugLogUtil.d("Commit actions disposed [user scope: ${userScopeProvider.hashCode()}]")
                finalizePool()
            }
            .flatMap {
                Completable.fromCallable { local.deleteUsedActionObjects() }
                           .toSingleDefault(it.lastActionTime)
            }
}
