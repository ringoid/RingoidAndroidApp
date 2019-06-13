package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import io.reactivex.Single
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong

abstract class BarrierActionObjectPool(cloud: RingoidCloud, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    private val triggerInProgress = Semaphore(1)

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Not synchronized method, because synchronization is achieved via semaphore.
     */
    override fun triggerSource(): Single<Long> {
        fun threadStr(thread: ProcessingPayload) = if (BuildConfig.IS_STAGING) "[t=${Thread.currentThread().id} n=${Thread.currentThread().name} / $tcount (${triggerInProgress.queueLength})] at ${thread.startTime} ms" else "[t=${Thread.currentThread().id} / $tcount]"

        return Single.just(ProcessingPayload())
            .flatMap { thread ->
                tcount.incrementAndGet()
                DebugLogUtil.v("Acquiring permission to commit actions by ${threadStr(thread)}")
                // TODO: sometimes deadlocks
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                DebugLogUtil.v("Permission's been acquired to commit actions by ${threadStr(thread)}")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit actions has started by ${threadStr(thread)}") }
                    .doOnError { DebugLogUtil.e("Commit actions has failed by ${threadStr(thread)}") }
                    .doFinally {
                        triggerInProgress.release()
                        tcount.decrementAndGet()
                        DebugLogUtil.v("Commit actions has finished by ${threadStr(thread)}, elapsed time ${System.currentTimeMillis() - thread.startTime} ms")
                    }
            }
    }

    private var tcount: AtomicLong = AtomicLong(0L)  // count of threads

    // --------------------------------------------------------------------------------------------
    override fun finalizePool() {
        super.finalizePool()
        // TODO: on dispose some thread, semaphore could be locked by it and never disposed
        // TODO: check whether it is locked and if so - release
//        triggerInProgress.release()
    }
}
