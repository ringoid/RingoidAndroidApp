package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.api.RingoidCloud
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
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
    override fun triggerSource(): Single<Long> =
        Single.just(ProcessingPayload())
            .flatMap { thread ->
                tcount.incrementAndGet()
                DebugLogUtil.v("Acquiring permission to commit actions by ${threadStr(thread)}")
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                DebugLogUtil.v("Permission's been acquired to commit actions by ${threadStr(thread)}")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit actions has started by ${threadStr(thread)}") }
                    .doOnError { DebugLogUtil.e("Commit actions has failed by ${threadStr(thread)} with $it") }
                    .doFinally {
                        finishTriggerSource()  // 'doFinally' must be thread-safe
                        DebugLogUtil.v("Commit actions has finished by ${threadStr(thread)}, elapsed time ${System.currentTimeMillis() - thread.startTime} ms")
                    }
            }

    @Synchronized
    private fun finishTriggerSource() {
        triggerInProgress.release()
        tcount.decrementAndGet()
        SentryUtil.breadcrumb("Released lock by thread: ${threadInfo()}")
    }

    // ------------------------------------------
    private var tcount: AtomicLong = AtomicLong(0L)  // count of threads

    protected fun threadInfo(): String = if (BuildConfig.IS_STAGING) "[t=${Thread.currentThread().id} n=${Thread.currentThread().name} / $tcount (${triggerInProgress.queueLength})]" else "[t=${Thread.currentThread().name} / $tcount]"
    private fun threadStr(thread: ProcessingPayload) = if (BuildConfig.IS_STAGING) "${threadInfo()} at ${thread.startTime} ms" else "[t=${Thread.currentThread().name} / $tcount]"
}
