package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
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
        fun threadStr(thread: Pair<ProcessingPayload, Long>) = "[t=${Thread.currentThread().id} n=${Thread.currentThread().name} / $tcount] at ${thread.first.startTime} ms"

        return Single.just(ProcessingPayload() to tcount.incrementAndGet())
            .flatMap { thread ->
                DebugLogUtil.v("Commit: acquiring permission to commit actions by ${threadStr(thread)}")
                // TODO: sometimes deadlocks
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                DebugLogUtil.v("Commit: just got permission to commit actions by ${threadStr(thread)}")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit: commit actions has started by ${threadStr(thread)}") }
                    .doOnError { DebugLogUtil.e("Commit: commit actions has failed by ${threadStr(thread)}") }
                    .doFinally {
                        triggerInProgress.release()
                        tcount.decrementAndGet()
                        DebugLogUtil.v("Commit: commit actions has finished by ${threadStr(thread)}, elapsed time ${System.currentTimeMillis() - thread.first.startTime} ms")
                    }
            }
    }

    private var tcount: AtomicLong = AtomicLong(0L)  // count of threads
}
