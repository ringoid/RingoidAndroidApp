package com.ringoid.domain.model

import com.ringoid.domain.model.messenger.IMessage

fun Collection<IMessage>.print(): String =
        "[$size]: ${joinToString(", ", "{", "}", transform = { it.text })}"
