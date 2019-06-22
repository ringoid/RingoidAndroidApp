package com.ringoid.domain.model

import com.ringoid.domain.model.messenger.IMessage

fun Collection<IMessage>.print(n: Int = 2): String =
        "[$size]: ${joinToString(", ", "{", "}",
                transform = { it.text.takeIf { it.length > n }?.substring(0..n) ?: it.text })}"
