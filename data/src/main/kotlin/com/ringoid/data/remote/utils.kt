package com.ringoid.data.remote

import com.ringoid.domain.model.IEssence
import okhttp3.MediaType
import okhttp3.RequestBody

fun IEssence.toBody(): RequestBody = RequestBody.create(MediaType.parse("application/json"), this.toJson())
