package com.ringoid.domain.exception

import timber.log.Timber

class ThresholdExceededException : RuntimeException("Threshold exceeded") {

    init {
        Timber.e("ThresholdExceededException: $message")
    }
}
