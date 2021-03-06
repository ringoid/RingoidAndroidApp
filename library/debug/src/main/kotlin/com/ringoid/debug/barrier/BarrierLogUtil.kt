package com.ringoid.debug.barrier

import com.ringoid.debug.model.BarrierLogItem
import com.ringoid.utility.DebugOnly
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Designed to be accessed from single thread, that actually acquired and still holds mutex.
 * Dumps logs into local cache periodically.
 */
@DebugOnly
object BarrierLogUtil {

    private val cachingInProgress = Semaphore(1, true)

    private var dao: IBarrierLogDaoHelper? = null
    private val logs = mutableListOf<BarrierLogItem>()

    fun c(log: String) {
        if (cachingInProgress.tryAcquire(0, TimeUnit.SECONDS)) {
            logs.clear()
            logs.add(BarrierLogItem(log = log))
            cachingInProgress.release()
        }
    }

    fun v(log: String) {
        if (cachingInProgress.tryAcquire(0, TimeUnit.SECONDS)) {
            logs.add(BarrierLogItem(log = log))
            cachingInProgress.release()
        }
    }

    // ------------------------------------------
    fun connectToDb(dao: IBarrierLogDaoHelper) {
        BarrierLogUtil.dao = dao
        persistLogsPeriodically()  // start timer to persist logs periodically
    }

    fun getLog(): Single<List<BarrierLogItem>>? =
        dao?.log()
           ?.subscribeOn(Schedulers.io())
           ?.concatWith {
               Flowable.fromIterable(logs)
                   .doOnSubscribe { cachingInProgress.tryAcquire(0, TimeUnit.SECONDS) }
                   .doFinally { cachingInProgress.release() }
           }
           ?.collect({ mutableListOf<BarrierLogItem>() }, { out, items -> out.addAll(items) })
           ?.map { it.toList() }
           ?.observeOn(AndroidSchedulers.mainThread())

    @Suppress("CheckResult")
    private fun persistLogsPeriodically() {
        Single.just(0L)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable {
                cachingInProgress.tryAcquire(0, TimeUnit.SECONDS)
                val source = if (logs.isNotEmpty()) {
                    val xlogs = ArrayList(logs)
                    logs.clear()
                    Completable.fromCallable { dao?.addLogs(xlogs) }
                        .doOnSubscribe {
                            cachingInProgress.tryAcquire(0, TimeUnit.SECONDS)
                            Timber.v("Start caching barrier logs [${xlogs.size}]")
                        }
                        .doOnComplete { Timber.v("Finished caching barrier logs") }
                        .doFinally { cachingInProgress.release() }
                } else {
                    Completable.complete()
                }
                cachingInProgress.release()
                source
            }
            .repeatWhen { it.flatMap { Flowable.timer(12, TimeUnit.SECONDS, Schedulers.io()) } }
            .subscribe({}, Timber::e)
    }
}
