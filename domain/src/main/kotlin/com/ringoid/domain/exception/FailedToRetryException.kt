package com.ringoid.domain.exception

import timber.log.Timber

class FailedToRetryException(message: String) : RuntimeException(message) {

    init {
        Timber.e(this, message)
    }
}
