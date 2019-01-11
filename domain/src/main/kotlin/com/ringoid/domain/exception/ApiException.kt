package com.ringoid.domain.exception

import timber.log.Timber

class ApiException(val code: String, message: String? = null) : RuntimeException("code=$code: $message") {

    init {
        Timber.e(this, "ApiException: code=$code: $message")
    }
}
