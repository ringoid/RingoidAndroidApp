package com.ringoid.data.misc

import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.BuildConfig

fun Collection<MessageDbo>.printDbo(): String =
    "[$size]${if (BuildConfig.IS_STAGING) ": ${joinToString(", ", "{", "}", transform = { "(${it.id.substring(0..3)},${it.sourceFeed})${it.text}" })}" else ""}"
