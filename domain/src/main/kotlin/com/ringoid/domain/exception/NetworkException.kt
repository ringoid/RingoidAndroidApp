package com.ringoid.domain.exception

import timber.log.Timber

class NetworkException(val code: Int, val tag: String? = null) : RuntimeException("code=$code") {

    init {
        Timber.e(this, "NetworkException[tag=$tag]: code=$code: $message")
    }
}
