package com.ringoid.data.remote.debug

import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.debug.ICloudDebug
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton @DebugOnly
class CloudDebug @Inject constructor() : ICloudDebug {

    internal val debugParams = mutableMapOf<String, String>()

    /* API */
    // --------------------------------------------------------------------------------------------
    override fun get(key: String): String? = debugParams[key]
}

internal inline fun <reified T> Single<T>.keepDataForDebug(cloudDebug: ICloudDebug, vararg data: Pair<String, String>): Single<T> =
    doOnSubscribe { data.forEach { (k, v) -> (cloudDebug as CloudDebug).debugParams[k] = v } }
