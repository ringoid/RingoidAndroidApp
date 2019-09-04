package com.ringoid.domain.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ringoid.utility.DebugOnly

interface IEssence {

    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
            .toJson(this)

    fun toJson(gson: Gson): String = gson.toJson(this)

    @DebugOnly
    fun toDebugData(key: String? = "d"): Pair<String, String> = (key ?: javaClass.simpleName) to toDebugPayload()
    @DebugOnly
    fun toDebugPayload(): String = toSentryPayload()

    fun toSentryData(): Pair<String, String> = javaClass.simpleName to toSentryPayload()
    fun toSentryPayload(): String = "${hashCode()}"
}
