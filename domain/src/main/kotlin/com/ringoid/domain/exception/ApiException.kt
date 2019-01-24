package com.ringoid.domain.exception

import timber.log.Timber

class ApiException(val code: String, message: String? = null, val tag: String? = null) : RuntimeException("code=$code: $message") {

    init {
        Timber.e(this, "ApiException[tag=$tag]: code=$code: $message")
    }

    companion object {
        const val INVALID_ACCESS_TOKEN = "InvalidAccessTokenClientError"
        const val OLD_APP_VERSION = "TooOldAppVersionClientError"
        const val CLIENT_ERROR = "WrongRequestParamsClientError"
        const val SERVER_ERROR = "InternalServerError"
    }
}
