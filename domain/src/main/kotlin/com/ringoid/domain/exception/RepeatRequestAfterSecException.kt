package com.ringoid.domain.exception

import timber.log.Timber

class RepeatRequestAfterSecException(val delay: Long) : RuntimeException("Repeat request after delay: $delay") {

    init {
        Timber.e(this, "RepeatRequestAfterSecException: delay=$delay: $message")
    }
}
