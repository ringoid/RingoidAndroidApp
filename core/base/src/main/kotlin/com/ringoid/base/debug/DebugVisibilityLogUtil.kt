package com.ringoid.base.debug

import com.ringoid.base.view.VisibleHint
import com.ringoid.domain.BuildConfig
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.tagLine
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@DebugOnly
data class DebugVisibilityHintItem(val tag: String, val hint: VisibleHint)

@DebugOnly
object DebugVisibilityLogUtil {

    private val logger = ReplaySubject.createWithTimeAndSize<DebugVisibilityHintItem>(15, TimeUnit.SECONDS, Schedulers.newThread(), 10)
    fun loggerSource(): Observable<DebugVisibilityHintItem> = logger.hide()

    fun log(tag: String, hint: VisibleHint) {
        log(DebugVisibilityHintItem(tag = tag, hint = hint))
    }

    fun log(log: DebugVisibilityHintItem) {
        if (BuildConfig.DEBUG) {
            tagLine(prefix = " {Vis Log} ")
            Timber.d("${log.tag}: ${log.hint}")
            logger.onNext(log)
        }
    }
}
