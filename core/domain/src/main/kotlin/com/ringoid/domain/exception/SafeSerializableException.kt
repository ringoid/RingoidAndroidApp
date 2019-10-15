package com.ringoid.domain.exception

class SafeSerializableException(causeClass: String, message: String) : RuntimeException("[$causeClass]: $message")
