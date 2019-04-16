package com.ringoid.data.action_storage

import com.ringoid.data.local.shared_prefs.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.debug.DebugLogUtil
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

abstract class BarrierActionObjectPool(cloud: RingoidCloud, spm: SharedPrefsManager)
    : BaseActionObjectPool(cloud, spm) {

    private val triggerInProgress = TriggerSemaphore()

    protected abstract fun triggerSourceImpl(): Single<Long>

    /**
     * Not synchronized method, because synchronization is achieved via semaphore.
     */
    override fun triggerSource(): Single<Long> =
        Single.just(ProcessingPayload(threadId = tid++) to tcount++)
            .flatMap { thread ->
                if (triggerInProgress.isLocked()) {
                    Single.error(WaitUntilTriggerFinishedException(tpayload = thread.first))
                } else {
                    triggerInProgress.increment()
                    triggerSourceImpl()
                        .doOnSubscribe { DebugLogUtil.d("Commit actions started by [t=${thread.first.threadId}] at ${thread.first.startTime % 1000000} ms") }
                        .doFinally {
                            triggerInProgress.decrement()
                            --tcount
                            DebugLogUtil.d("Commit actions has finished by [t=${thread.first.threadId}], elapsed time ${System.currentTimeMillis() - thread.first.startTime} ms")
                        }
                }
            }
            .retryWhen {
                it.flatMap { e ->
                    if (e is WaitUntilTriggerFinishedException) {
                        DebugLogUtil.v("${e.message}, count $tcount")
                        Flowable.timer(200L, TimeUnit.MILLISECONDS)  // repeat
                    } else {
                        DebugLogUtil.e(e)
                        Flowable.error(e)
                    }
                }
            }

    private var tid: Long = 0L  // thread id
    private var tcount: Long = 0L  // count of threads

    // ------------------------------------------
    override fun finalizePool() {
        super.finalizePool()
        triggerInProgress.drop()
    }
}
