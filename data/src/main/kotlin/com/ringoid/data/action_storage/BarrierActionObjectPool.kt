package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.log.SentryUtil
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
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
                val tid = checkForPotentialDeadlock(thread)
                triggerInProgress.acquireUninterruptibly()  // acquire permission to continue, or block on a barrier otherwise
                DebugLogUtil.v("Permission's been acquired to commit actions by ${threadStr(thread)}")
                triggerSourceImpl()
                    .doOnSubscribe { DebugLogUtil.v("Commit actions has started by ${threadStr(thread)}") }
                    .doOnError { DebugLogUtil.e("Commit actions has failed by ${threadStr(thread)}") }
                    .doFinally {
                        finishTriggerSource(callerThreadId = tid)  // 'doFinally' must be thread-safe
                        DebugLogUtil.v("Commit actions has finished by ${threadStr(thread)}, elapsed time ${System.currentTimeMillis() - thread.startTime} ms")
                    }
            }

    @Synchronized
    private fun finishTriggerSource(callerThreadId: Long) {
        triggerInProgress.release()
        tcount.decrementAndGet()
        table.remove(callerThreadId)
    }

    private var tcount: AtomicLong = AtomicLong(0L)  // count of threads

    private fun threadStr(thread: ProcessingPayload) = if (BuildConfig.IS_STAGING) "[t=${Thread.currentThread().id} n=${Thread.currentThread().name} / $tcount (${triggerInProgress.queueLength})] at ${thread.startTime} ms" else "[t=${Thread.currentThread().id} / $tcount]"

    // --------------------------------------------------------------------------------------------
    private val stats = mutableListOf<Long>()
    private val table = mutableMapOf<Long, ProcessingPayload>()
    private val timer: Disposable =  // periodic timer checking threads' table
        Observable.interval(3, TimeUnit.SECONDS)
            .doOnNext {
                val elapsedTimes = table.map { System.currentTimeMillis() - it.value.startTime }.sorted()
                if (elapsedTimes.isNotEmpty()) {
                    stats.add(elapsedTimes[elapsedTimes.size / 2])
                }
                if (stats.size > 30) {
                    val sorted = stats.sorted()
                    val median = sorted[sorted.size / 2]
                    if (median >= 4000) {
                        SentryUtil.e("Potential deadlock", listOf("where" to "aobj queue",
                                     "check" to "elapsed times", "queue length" to "${triggerInProgress.queueLength}",
                                     "thread count" to "$tcount", "median time" to "$median"))
                    }
                }
            }
            .subscribe({}, { SentryUtil.capture(it, message = "Deadlock Guard timer error") })

    @Synchronized
    private fun checkForPotentialDeadlock(thread: ProcessingPayload): Long {
        val tid = Thread.currentThread().id
        table[tid] = thread  // writing to table is thread-safe due to 'synchronized'

        val queueLength = triggerInProgress.queueLength
        val count = tcount.get()
        if (queueLength > 5 || count > 5) {
            SentryUtil.e("Potential deadlock", listOf("where" to "aobj queue", "check" to "counts",
                         "queue length" to "$queueLength", "thread count" to "$count"))
        }

        return tid
    }
}
