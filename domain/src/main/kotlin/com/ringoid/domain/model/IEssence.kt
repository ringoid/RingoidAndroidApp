package com.ringoid.domain.model

import com.google.gson.Gson

interface IEssence {

    fun toJson(): String = Gson().toJson(this)
    fun toJson(gson: Gson): String = gson.toJson(this)
}
