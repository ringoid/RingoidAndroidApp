package com.ringoid.data.remote.debug

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.ICloudDebug
import io.reactivex.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton @DebugOnly
class CloudDebug @Inject constructor() : ICloudDebug {

    internal val debugParams = mutableMapOf<String, String>()

    /* API */
    // --------------------------------------------------------------------------------------------
    override fun get(key: String): String? = debugParams[key]
}

internal fun Completable.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Completable =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }
internal inline fun <reified T> Maybe<T>.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Maybe<T> =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }
internal inline fun <reified T> Single<T>.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Single<T> =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }
internal inline fun <reified T> Flowable<T>.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Flowable<T> =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }
internal inline fun <reified T> Observable<T>.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Observable<T> =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }

internal inline fun <reified T> Maybe<T>.keepResultForDebug(cloudDebug: ICloudDebug): Maybe<T> =
    doOnSuccess { (cloudDebug as CloudDebug).debugParams["result"] = "$it" }
internal inline fun <reified T> Single<T>.keepResultForDebug(cloudDebug: ICloudDebug): Single<T> =
    doOnSuccess { (cloudDebug as CloudDebug).debugParams["result"] = "$it" }
internal inline fun <reified T> Flowable<T>.keepResultForDebug(cloudDebug: ICloudDebug): Flowable<T> =
    doOnNext { (cloudDebug as CloudDebug).debugParams["result"] = "$it" }
internal inline fun <reified T> Observable<T>.keepResultForDebug(cloudDebug: ICloudDebug): Observable<T> =
    doOnNext { (cloudDebug as CloudDebug).debugParams["result"] = "$it" }
