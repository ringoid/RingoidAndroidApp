package com.ringoid.report.exception

sealed class NetworkUnexpected(val code: String) : RuntimeException("Fatal network exception: $code") {

    companion object {
        const val ERROR_CONNECTION_FATAL = "ErrorConnectionFatal"
        const val ERROR_CONNECTION_INSECURE = "ErrorConnectionInsecure"
        const val ERROR_CONNECTION_TIMED_OUT = "ErrorConnectionTimedOut"
        const val ERROR_NO_CONNECTION = "ErrorNoConnection"

        fun from(code: String): NetworkUnexpected =
            when (code) {
                ERROR_CONNECTION_FATAL -> ErrorConnectionFatal()
                ERROR_CONNECTION_INSECURE -> ErrorConnectionInsecure()
                ERROR_CONNECTION_TIMED_OUT -> ErrorConnectionTimedOut()
                ERROR_NO_CONNECTION -> ErrorNoConnection()
                else -> UnspecifiedNetworkError()
            }
    }
}

class ErrorConnectionFatal : NetworkUnexpected(ERROR_CONNECTION_FATAL)
class ErrorConnectionInsecure : NetworkUnexpected(ERROR_CONNECTION_INSECURE)
class ErrorConnectionTimedOut : NetworkUnexpected(ERROR_CONNECTION_TIMED_OUT)
class ErrorNoConnection : NetworkUnexpected(ERROR_NO_CONNECTION)
class UnspecifiedNetworkError : NetworkUnexpected("")
