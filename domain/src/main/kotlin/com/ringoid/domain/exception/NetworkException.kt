package com.ringoid.domain.exception

class NetworkException(val code: Int) : RuntimeException("code=$code")
