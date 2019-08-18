package com.ringoid.data.action_storage

import com.ringoid.data.handleError
import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.local.shared_prefs.accessSingle
import com.ringoid.datainterface.local.action_storage.IActionObjectDbFacade
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.datainterface.remote.model.actions.CommitActionsResponse
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import com.ringoid.domain.model.actions.OriginActionObject
import com.ringoid.domain.model.essence.action.CommitActionsEssence
import com.ringoid.domain.scope.UserScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistActionObjectPool @Inject constructor(
    private val local: IActionObjectDbFacade,
    private val userScopeProvider: UserScopeProvider,
    cloud: IRingoidCloudFacade, spm: SharedPrefsManager)
    : BarrierActionObjectPool(cloud, spm) {

    // ------------------------------------------------------------------------
    override fun getTotalQueueSize(): Int = 0  // don't trigger by capacity hit

    override fun countActionObjects(): Single<Int> = local.countActionObjects()

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult")
    override fun put(aobj: OriginActionObject, onComplete: (() -> Unit)?) {
        putSource(aobj)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { onComplete?.invoke() }
            .autoDisposable(userScopeProvider)
            .subscribe({ DebugLogUtil.v("Put single aobj completed") }, Timber::e)
    }

    @Suppress("CheckResult")
    override fun put(aobjs: Collection<OriginActionObject>, onComplete: (() -> Unit)?) {
        putSource(aobjs)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { onComplete?.invoke() }
            .autoDisposable(userScopeProvider)
            .subscribe({ DebugLogUtil.v("Put aobjs completed") }, Timber::e)
    }

    private fun putSource(aobj: OriginActionObject): Completable =
        Completable.fromCallable {
            local.addActionObject(aobj)
            analyzeActionObject(aobj)
        }
        .doOnSubscribe {
            Timber.v("Put action object: $aobj")
            DebugLogUtil.v("Put single action object: ${aobj.actionType}")
        }

    private fun putSource(aobjs: Collection<OriginActionObject>): Completable =
        Completable.fromCallable {
            local.addActionObjects(aobjs)
            aobjs.forEach { analyzeActionObject(it) }
        }
        .doOnSubscribe {
            Timber.v("Put action objects [${aobjs.size}]: ${aobjs.joinToString()}")
            DebugLogUtil.v("Put [${aobjs.size}] action objects: ${aobjs.joinToString { it.actionType }}")
        }

    // ------------------------------------------
    override fun commitNow(aobj: OriginActionObject): Single<Long> =
        Single.fromCallable { local.addActionObject(aobj) }
              .flatMap { triggerSource() }

    @Suppress("CheckResult")
    override fun trigger() {
        Timber.d("Trigger standalone")
        local.countActionObjects()
            .subscribeOn(Schedulers.newThread())
            .flatMap { count ->
                if (count <= 0) {
                    DebugLogUtil.d("No actions to commit, lAt is up-to-date [standalone]")
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
                if (count <= 0) {
                    DebugLogUtil.d("No actions to commit, lAt is up-to-date [chained]")
                    Single.just(CommitActionsResponse(lastActionTime()))  // do nothing on empty queue
                } else {
                    local.actionObjectsMarkAsUsed()
                        .flatMap { queue ->
                            spm.accessSingle {
                                /**
                                 * @see [ActionObjectPool.triggerSourceImpl] for explanation.
                                 */
                                val queueCopy = ArrayDeque(queue)
                                val essence = CommitActionsEssence(it.accessToken, queueCopy)
                                cloud.commitActions(essence)
                                     .handleError(tag = "commitActions", traceTag = "actions/actions", count = 8)                       }
                    }
                }
            }
            .doOnError {
                SentryUtil.breadcrumb("Commit actions error",
                    "exception" to "${it.javaClass}", "message" to "${it.message}")
                DebugLogUtil.e("Commit actions error: $it ;; ${threadInfo()}")
            }
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
