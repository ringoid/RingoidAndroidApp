package com.ringoid.domain.exception

class NetworkException(val code: Int, val tag: String? = null) : RuntimeException("code=$code")
