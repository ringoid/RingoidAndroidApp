package com.ringoid.origin.push

enum class PushType { DEEPLINK, MESSAGE, SYSTEM, UNKNOWN }

fun parsePushType(string: String?): PushType =
    when (string) {
        "deeplink" -> PushType.DEEPLINK
        "message" -> PushType.MESSAGE
        "system" -> PushType.SYSTEM
        else -> PushType.UNKNOWN
    }

