package com.ringoid.domain.model

import com.ringoid.domain.BuildConfig
import com.ringoid.domain.model.messenger.IMessage

fun Collection<IMessage>.print(): String =
        "[$size]${if (BuildConfig.IS_STAGING) ": ${joinToString(", ", "{", "}", transform = { it.text })}" else ""}"

fun List<IMessage>.print(n: Int): String = if (size <= n) print() else "[$size]-${subList(0, n).print()}"
