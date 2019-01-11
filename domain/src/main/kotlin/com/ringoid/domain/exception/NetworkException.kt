package com.ringoid.domain.exception

import timber.log.Timber

class NetworkException(val code: Int) : RuntimeException("code=$code") {

    init {
        Timber.e(this, "NetworkException: code=$code: $message")
    }
}
