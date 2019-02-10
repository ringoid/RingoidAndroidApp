package com.ringoid.domain.exception

class NetworkUnexpected(val code: String) : RuntimeException("Fatal network exception: $code")
