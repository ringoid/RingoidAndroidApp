package com.ringoid.domain.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder

interface IEssence {

    fun toJson(): String =
        GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
            .toJson(this)

    fun toJson(gson: Gson): String = gson.toJson(this)
}
