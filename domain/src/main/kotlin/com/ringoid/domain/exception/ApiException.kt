package com.ringoid.domain.exception

class ApiException(val code: String, message: String? = null) : RuntimeException("code=$code: $message")
