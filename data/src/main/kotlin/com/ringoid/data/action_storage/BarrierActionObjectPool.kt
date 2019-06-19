package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.exception.DeadlockException
import com.ringoid.domain.log.SentryUtil
import io.reactivex.Single
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong

abstract class BarrierActionObjectPool(cloud: RingoidCloud, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    private val triggerInProgress = Semaphore(1)
    private var lockOwnerThreadId: Long = DomainUtil.BAD_VALUE_L

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Not synchronized method, because synchronization is achieved via semaphore.
     */
    override fun triggerSource(): Single<Long> =
        Single.just(ProcessingPayload())
            .flatMap { thread ->
                tcount.incrementAndGet()
                DebugLogUtil.v("Acquiring permission to commit actions by ${threadStr(thread)}")
                if (Thread.currentThread().id == lockOwnerThreadId) {
                    SentryUtil.capture(DeadlockException(), "Deadlock in commit actions",
                        tag = "interrupted = ${Thread.currentThread().isInterrupted}",
                        extras = Thread.currentThread().stackTrace.map { it.className to "${it.methodName}:${it.lineNumber}" })
                }
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                lockOwnerThreadId = Thread.currentThread().id
                DebugLogUtil.v("Permission's been acquired to commit actions by ${threadStr(thread)}")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit actions has started by ${threadStr(thread)}") }
                    .doOnError { DebugLogUtil.e("Commit actions has failed by ${threadStr(thread)}") }
                    .doFinally {
                        finishTriggerSource()  // 'doFinally' must be thread-safe
                        DebugLogUtil.v("Commit actions has finished by ${threadStr(thread)}, elapsed time ${System.currentTimeMillis() - thread.startTime} ms")
                    }
            }

    @Synchronized
    private fun finishTriggerSource() {
        triggerInProgress.release()
        tcount.decrementAndGet()
        SentryUtil.breadcrumb("Released lock by thread $lockOwnerThreadId")
        lockOwnerThreadId = DomainUtil.BAD_VALUE_L
    }

    private var tcount: AtomicLong = AtomicLong(0L)  // count of threads

    private fun threadStr(thread: ProcessingPayload) = if (BuildConfig.IS_STAGING) "[t=${Thread.currentThread().id} n=${Thread.currentThread().name} / $tcount (${triggerInProgress.queueLength})] at ${thread.startTime} ms" else "[t=${Thread.currentThread().id} / $tcount]"
}
