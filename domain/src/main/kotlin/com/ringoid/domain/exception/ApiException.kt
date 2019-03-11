package com.ringoid.domain.exception

import timber.log.Timber

open class ApiException(val code: String, message: String? = null, val tag: String? = null) : RuntimeException("code=$code: $message") {

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

open class InvalidAccessTokenApiException(message: String? = null, tag: String? = null)
    : ApiException(code = INVALID_ACCESS_TOKEN, message = message, tag = tag)

class InternalServerErrorApiException(message: String? = null, tag: String? = null)
    : ApiException(code = SERVER_ERROR, message = message, tag = tag)

class OldAppVersionApiException(message: String? = null, tag: String? = null)
    : ApiException(code = OLD_APP_VERSION, message = message, tag = tag)

class WrongRequestParamsClientApiException(message: String? = null, tag: String? = null)
    : ApiException(code = CLIENT_ERROR, message = message, tag = tag)
