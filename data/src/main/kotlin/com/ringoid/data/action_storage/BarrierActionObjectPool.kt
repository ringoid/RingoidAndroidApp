package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.debug.DebugLogUtil
import io.reactivex.Single
import java.util.concurrent.Semaphore

abstract class BarrierActionObjectPool(cloud: RingoidCloud, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    private val triggerInProgress = Semaphore(1)

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Not synchronized method, because synchronization is achieved via semaphore.
     */
    override fun triggerSource(): Single<Long> =
        Single.just(ProcessingPayload(threadId = tid++) to tcount++)
            .flatMap { thread ->
                DebugLogUtil.v("Acquiring permission to commit actions by [t=${thread.first.threadId} / $tcount] at ${thread.first.startTime} ms")
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                DebugLogUtil.v("Thread [t=${thread.first.threadId}] has just got permission to commit actions")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit actions has started by [t=${thread.first.threadId} / $tcount] at ${thread.first.startTime} ms") }
                    .doFinally {
                        triggerInProgress.release()
                        --tcount
                        DebugLogUtil.v("Commit actions has finished by [t=${thread.first.threadId} / $tcount], elapsed time ${System.currentTimeMillis() - thread.first.startTime} ms")
                    }
            }

    private var tid: Long = 1000L  // thread id
    private var tcount: Long = 0L  // count of threads
}
