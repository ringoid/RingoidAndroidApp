package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.debug.DebugLogUtil
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class DelayActionObjectPool(cloud: IRingoidCloudFacade, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    companion object {
        const val MIN_DELAY_TRIGGER = 100L  // ms
        const val MAX_DELAY_TRIGGER = 200L  // ms
    }

    private var tcount = AtomicInteger(1)  // count of threads
    private val triggerInProgress = AtomicBoolean(false)

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Thread performs [triggerSourceImpl], others are delayed until the first one finishes.
     * Delays thread for a short period of time, then check whether the job is still in progress,
     * and if so - delay again, otherwise - start doing the job.
     */
    override fun triggerSource(): Single<Long> =
        countActionObjects()
            .flatMap { count ->
                when {
                    count <= 0 -> {  // no job to do for this thread
                        DebugLogUtil.d("No actions to commit, lAt is up-to-date [chained]")
                        Single.just(lastActionTime())
                    }
                    triggerInProgress.get() ->  // job is in progress by some other thread
                        Single.just(0L)
                              .doOnSubscribe {
                                  val tasksCount = tcount.incrementAndGet()
                                  DebugLogUtil.v("Waiting for commit actions to finish [$tasksCount]...")
                              }
                              .delay((MIN_DELAY_TRIGGER..MAX_DELAY_TRIGGER).random(), TimeUnit.MILLISECONDS)
                              .flatMap { triggerSource() }
                    else -> triggerSourceImpl()  // this thread should do the job
                        .doOnSubscribe {
                            triggerInProgress.set(true)  // acquire non-blocking 'lock'
                            tcount.decrementAndGet()
                        }
                        .doFinally { triggerInProgress.set(false) }  // release non-blocking 'lock'
                }
            }
}
